package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.SupplyRecord;

public interface SupplyRecordService extends IService<SupplyRecord> {
    Page<SupplyRecord> pageQuery(Long current, Long size, Long workerId, Long supplyId, String issueStatus);

    SupplyRecord confirmIssue(SupplyRecord record);
}
