package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.WorkHour;

import java.util.List;

public interface WorkHourService extends IService<WorkHour> {
    Page<WorkHour> pageQuery(Long current, Long size, String settleMonth, Long workerId, String settleStatus);

    List<WorkHour> settleMonth(String settleMonth);

    WorkHour settleWorker(String settleMonth, Long workerId);
}
