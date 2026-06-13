package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Position;
import com.scaffolding.entity.Schedule;
import com.scaffolding.mapper.ScheduleMapper;
import com.scaffolding.service.HeatConfigService;
import com.scaffolding.service.PositionService;
import com.scaffolding.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Autowired
    private HeatConfigService heatConfigService;

    @Autowired
    private PositionService positionService;

    @Override
    public Page<Schedule> pageQuery(Long current, Long size, String scheduleDate, Long workerId, String positionType, String scheduleStatus) {
        Page<Schedule> page = new Page<>(current, size);
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(scheduleDate)) {
            wrapper.eq(Schedule::getScheduleDate, scheduleDate);
        }
        if (workerId != null) {
            wrapper.eq(Schedule::getWorkerId, workerId);
        }
        if (StringUtils.hasText(positionType)) {
            wrapper.eq(Schedule::getPositionType, positionType);
        }
        if (StringUtils.hasText(scheduleStatus)) {
            wrapper.eq(Schedule::getScheduleStatus, scheduleStatus);
        }

        wrapper.orderByDesc(Schedule::getScheduleDate);
        return this.page(page, wrapper);
    }

    @Override
    public Schedule saveWithHeatCheck(Schedule schedule) {
        if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
            schedule.setWorkHours(calcWorkHours(schedule));
        }
        if (schedule.getPositionId() != null) {
            Position position = positionService.getById(schedule.getPositionId());
            if (position != null) {
                schedule.setPositionName(position.getPositionName());
                schedule.setPositionType(position.getPositionType());
            }
        }
        applyHeatRule(schedule);
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());
        this.save(schedule);
        return schedule;
    }

    @Override
    public Schedule updateWithHeatCheck(Long id, Schedule schedule) {
        schedule.setId(id);
        if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
            schedule.setWorkHours(calcWorkHours(schedule));
        }
        if (schedule.getPositionId() != null) {
            Position position = positionService.getById(schedule.getPositionId());
            if (position != null) {
                schedule.setPositionName(position.getPositionName());
                schedule.setPositionType(position.getPositionType());
            }
        }
        applyHeatRule(schedule);
        schedule.setUpdateTime(LocalDateTime.now());
        this.updateById(schedule);
        return schedule;
    }

    @Override
    public BigDecimal calcWorkHours(Schedule schedule) {
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return BigDecimal.ZERO;
        }
        Duration duration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
        long minutes = duration.toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    @Override
    public void applyHeatRule(Schedule schedule) {
        if (!"outdoor".equals(schedule.getPositionType())) {
            schedule.setHeatWarning(0);
            schedule.setHeatAllowance(BigDecimal.ZERO);
            schedule.setRestFrequency(0);
            return;
        }

        String thresholdStr = heatConfigService.getConfigValue("heat_threshold");
        String stopTempStr = heatConfigService.getConfigValue("work_stop_temp");
        String restBelow37 = heatConfigService.getConfigValue("rest_frequency_below_37");
        String rest37to40 = heatConfigService.getConfigValue("rest_frequency_37_40");
        String restAbove40 = heatConfigService.getConfigValue("rest_frequency_above_40");

        BigDecimal threshold = new BigDecimal(thresholdStr != null ? thresholdStr : "35");
        BigDecimal stopTemp = new BigDecimal(stopTempStr != null ? stopTempStr : "40");

        if (schedule.getTemperature() == null) {
            schedule.setHeatWarning(0);
            schedule.setHeatAllowance(BigDecimal.ZERO);
            schedule.setRestFrequency(0);
            return;
        }

        int compareResult = schedule.getTemperature().compareTo(threshold);
        if (compareResult >= 0) {
            schedule.setHeatWarning(1);
            Position position = null;
            if (schedule.getPositionId() != null) {
                position = positionService.getById(schedule.getPositionId());
            }
            if (position != null && position.getHeatAllowance() != null) {
                schedule.setHeatAllowance(position.getHeatAllowance());
            } else {
                schedule.setHeatAllowance(new BigDecimal("30"));
            }

            BigDecimal temp = schedule.getTemperature();
            if (temp.compareTo(stopTemp) >= 0) {
                schedule.setRestFrequency(Integer.parseInt(restAbove40 != null ? restAbove40 : "6"));
            } else if (temp.compareTo(new BigDecimal("37")) >= 0) {
                schedule.setRestFrequency(Integer.parseInt(rest37to40 != null ? rest37to40 : "4"));
            } else {
                schedule.setRestFrequency(Integer.parseInt(restBelow37 != null ? restBelow37 : "3"));
            }
        } else {
            schedule.setHeatWarning(0);
            schedule.setHeatAllowance(BigDecimal.ZERO);
            schedule.setRestFrequency(0);
        }
    }
}
