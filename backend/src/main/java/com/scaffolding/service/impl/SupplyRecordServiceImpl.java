package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.HeatSupply;
import com.scaffolding.entity.SupplyRecord;
import com.scaffolding.mapper.SupplyRecordMapper;
import com.scaffolding.service.HeatSupplyService;
import com.scaffolding.service.SupplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SupplyRecordServiceImpl extends ServiceImpl<SupplyRecordMapper, SupplyRecord> implements SupplyRecordService {

    private static final AtomicInteger sequence = new AtomicInteger(0);

    @Autowired
    private HeatSupplyService heatSupplyService;

    @Override
    public Page<SupplyRecord> pageQuery(Long current, Long size, Long workerId, Long supplyId, String issueStatus) {
        Page<SupplyRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<SupplyRecord> wrapper = new LambdaQueryWrapper<>();

        if (workerId != null) {
            wrapper.eq(SupplyRecord::getWorkerId, workerId);
        }
        if (supplyId != null) {
            wrapper.eq(SupplyRecord::getSupplyId, supplyId);
        }
        if (issueStatus != null && !issueStatus.isEmpty()) {
            wrapper.eq(SupplyRecord::getIssueStatus, issueStatus);
        }

        wrapper.orderByDesc(SupplyRecord::getIssueTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyRecord confirmIssue(SupplyRecord record) {
        String recordNo = "SR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", sequence.incrementAndGet() % 10000);
        record.setRecordNo(recordNo);

        if (record.getSupplyId() != null) {
            HeatSupply supply = heatSupplyService.getById(record.getSupplyId());
            if (supply != null) {
                record.setSupplyName(supply.getSupplyName());
                record.setSupplyCode(supply.getSupplyCode());
                record.setUnit(supply.getUnit());

                int remain = supply.getStockQuantity() - (record.getQuantity() != null ? record.getQuantity() : 1);
                if (remain < 0) {
                    throw new RuntimeException("库存不足，当前库存：" + supply.getStockQuantity());
                }
                supply.setStockQuantity(remain);
                heatSupplyService.updateById(supply);
            }
        }

        if (record.getIssueTime() == null) {
            record.setIssueTime(LocalDateTime.now());
        }
        if (record.getIssueStatus() == null) {
            record.setIssueStatus("confirmed");
        }
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        this.save(record);
        return record;
    }
}
