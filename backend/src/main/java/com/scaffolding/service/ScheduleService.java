package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Schedule;

import java.math.BigDecimal;

public interface ScheduleService extends IService<Schedule> {
    Page<Schedule> pageQuery(Long current, Long size, String scheduleDate, Long workerId, String positionType, String scheduleStatus);

    Schedule saveWithHeatCheck(Schedule schedule);

    Schedule updateWithHeatCheck(Long id, Schedule schedule);

    BigDecimal calcWorkHours(Schedule schedule);

    void applyHeatRule(Schedule schedule);
}
