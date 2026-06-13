package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.WorkHour;
import com.scaffolding.service.WorkHourService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/work-hour")
@Api(tags = "工时工资结算")
public class WorkHourController {

    @Autowired
    private WorkHourService workHourService;

    @PostMapping("/settle-month")
    @ApiOperation("按月结算所有工人工资")
    public Result<List<WorkHour>> settleMonth(@RequestParam String settleMonth) {
        try {
            List<WorkHour> list = workHourService.settleMonth(settleMonth);
            return Result.success("结算成功", list);
        } catch (Exception e) {
            log.error("按月结算失败", e);
            return Result.error("结算失败：" + e.getMessage());
        }
    }

    @PostMapping("/settle-worker")
    @ApiOperation("结算指定工人某月工资")
    public Result<WorkHour> settleWorker(@RequestParam String settleMonth, @RequestParam Long workerId) {
        try {
            WorkHour wh = workHourService.settleWorker(settleMonth, workerId);
            return Result.success("结算成功", wh);
        } catch (Exception e) {
            log.error("结算工人工资失败", e);
            return Result.error("结算失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新结算记录")
    public Result<WorkHour> update(@PathVariable Long id, @RequestBody WorkHour workHour) {
        try {
            workHour.setId(id);
            workHour.setUpdateTime(LocalDateTime.now());
            workHourService.updateById(workHour);
            return Result.success("更新成功", workHour);
        } catch (Exception e) {
            log.error("更新结算记录失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除结算记录")
    public Result<?> delete(@PathVariable Long id) {
        try {
            workHourService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除结算记录失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询结算记录")
    public Result<WorkHour> getById(@PathVariable Long id) {
        WorkHour wh = workHourService.getById(id);
        if (wh == null) {
            return Result.error("记录不存在");
        }
        return Result.success(wh);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询结算记录")
    public Result<PageResult<WorkHour>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String settleMonth,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String settleStatus) {
        Page<WorkHour> page = workHourService.pageQuery(current, size, settleMonth, workerId, settleStatus);
        PageResult<WorkHour> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
}
