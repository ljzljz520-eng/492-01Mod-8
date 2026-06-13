package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.SupplyRecord;
import com.scaffolding.service.SupplyRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/supply-record")
@Api(tags = "物资发放记录")
public class SupplyRecordController {

    @Autowired
    private SupplyRecordService supplyRecordService;

    @PostMapping("/confirm")
    @ApiOperation("确认发放物资")
    public Result<SupplyRecord> confirm(@RequestBody SupplyRecord record) {
        try {
            SupplyRecord saved = supplyRecordService.confirmIssue(record);
            return Result.success("发放成功", saved);
        } catch (Exception e) {
            log.error("确认发放物资失败", e);
            return Result.error("发放失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除发放记录")
    public Result<?> delete(@PathVariable Long id) {
        try {
            supplyRecordService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除发放记录失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询发放记录")
    public Result<SupplyRecord> getById(@PathVariable Long id) {
        SupplyRecord record = supplyRecordService.getById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        return Result.success(record);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询发放记录")
    public Result<PageResult<SupplyRecord>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) Long supplyId,
            @RequestParam(required = false) String issueStatus) {
        Page<SupplyRecord> page = supplyRecordService.pageQuery(current, size, workerId, supplyId, issueStatus);
        PageResult<SupplyRecord> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
}
