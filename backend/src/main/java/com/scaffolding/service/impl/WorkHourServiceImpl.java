package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Position;
import com.scaffolding.entity.Schedule;
import com.scaffolding.entity.WorkHour;
import com.scaffolding.mapper.WorkHourMapper;
import com.scaffolding.service.PositionService;
import com.scaffolding.service.ScheduleService;
import com.scaffolding.service.WorkHourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WorkHourServiceImpl extends ServiceImpl<WorkHourMapper, WorkHour> implements WorkHourService {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private PositionService positionService;

    @Override
    public Page<WorkHour> pageQuery(Long current, Long size, String settleMonth, Long workerId, String settleStatus) {
        Page<WorkHour> page = new Page<>(current, size);
        LambdaQueryWrapper<WorkHour> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(settleMonth)) {
            wrapper.eq(WorkHour::getSettleMonth, settleMonth);
        }
        if (workerId != null) {
            wrapper.eq(WorkHour::getWorkerId, workerId);
        }
        if (StringUtils.hasText(settleStatus)) {
            wrapper.eq(WorkHour::getSettleStatus, settleStatus);
        }

        wrapper.orderByDesc(WorkHour::getSettleMonth);
        return this.page(page, wrapper);
    }

    @Override
    public List<WorkHour> settleMonth(String settleMonth) {
        YearMonth ym = YearMonth.parse(settleMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.between(Schedule::getScheduleDate, startDate, endDate);
        scheduleWrapper.in(Schedule::getScheduleStatus, "completed", "in_progress");
        List<Schedule> schedules = scheduleService.list(scheduleWrapper);

        Set<Long> workerIds = new HashSet<>();
        for (Schedule s : schedules) {
            if (s.getWorkerId() != null) {
                workerIds.add(s.getWorkerId());
            }
        }

        List<WorkHour> result = new ArrayList<>();
        for (Long workerId : workerIds) {
            WorkHour wh = settleWorker(settleMonth, workerId);
            result.add(wh);
        }

        return result;
    }

    @Override
    public WorkHour settleWorker(String settleMonth, Long workerId) {
        YearMonth ym = YearMonth.parse(settleMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getWorkerId, workerId);
        scheduleWrapper.between(Schedule::getScheduleDate, startDate, endDate);
        scheduleWrapper.in(Schedule::getScheduleStatus, "completed", "in_progress");
        List<Schedule> schedules = scheduleService.list(scheduleWrapper);

        BigDecimal normalHours = BigDecimal.ZERO;
        BigDecimal normalWage = BigDecimal.ZERO;
        BigDecimal heatHours = BigDecimal.ZERO;
        BigDecimal heatAllowanceTotal = BigDecimal.ZERO;
        int heatDays = 0;
        String workerName = null;

        for (Schedule s : schedules) {
            if (workerName == null) {
                workerName = s.getWorkerName();
            }
            BigDecimal hours = s.getWorkHours() != null ? s.getWorkHours() : BigDecimal.ZERO;
            BigDecimal positionHourlyWage = BigDecimal.ZERO;
            if (s.getPositionId() != null) {
                Position p = positionService.getById(s.getPositionId());
                if (p != null && p.getHourlyWage() != null) {
                    positionHourlyWage = p.getHourlyWage();
                }
            }

            if (s.getHeatWarning() != null && s.getHeatWarning() == 1) {
                heatDays++;
                heatHours = heatHours.add(hours);
                if (s.getHeatAllowance() != null) {
                    heatAllowanceTotal = heatAllowanceTotal.add(s.getHeatAllowance());
                }
                normalHours = normalHours.add(hours);
                normalWage = normalWage.add(hours.multiply(positionHourlyWage));
            } else {
                normalHours = normalHours.add(hours);
                normalWage = normalWage.add(hours.multiply(positionHourlyWage));
            }
        }

        normalWage = normalWage.setScale(2, RoundingMode.HALF_UP);
        heatAllowanceTotal = heatAllowanceTotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalWage = normalWage.add(heatAllowanceTotal).setScale(2, RoundingMode.HALF_UP);

        LambdaQueryWrapper<WorkHour> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(WorkHour::getSettleMonth, settleMonth);
        existWrapper.eq(WorkHour::getWorkerId, workerId);
        WorkHour exist = this.getOne(existWrapper);

        if (exist == null) {
            exist = new WorkHour();
            exist.setSettleMonth(settleMonth);
            exist.setWorkerId(workerId);
            exist.setWorkerName(workerName);
            exist.setNormalHours(normalHours);
            exist.setNormalWage(normalWage);
            exist.setHeatDays(heatDays);
            exist.setHeatHours(heatHours);
            exist.setHeatAllowanceTotal(heatAllowanceTotal);
            exist.setTotalWage(totalWage);
            exist.setSettleStatus("settled");
            exist.setCreateTime(java.time.LocalDateTime.now());
            exist.setUpdateTime(java.time.LocalDateTime.now());
            this.save(exist);
        } else {
            exist.setWorkerName(workerName);
            exist.setNormalHours(normalHours);
            exist.setNormalWage(normalWage);
            exist.setHeatDays(heatDays);
            exist.setHeatHours(heatHours);
            exist.setHeatAllowanceTotal(heatAllowanceTotal);
            exist.setTotalWage(totalWage);
            exist.setSettleStatus("settled");
            exist.setUpdateTime(java.time.LocalDateTime.now());
            this.updateById(exist);
        }

        return exist;
    }
}
