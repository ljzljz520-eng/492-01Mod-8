package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Schedule;
import com.scaffolding.service.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/schedule")
@Api(tags = "排班管理")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping
    @ApiOperation("新增排班")
    public Result<Schedule> save(@RequestBody Schedule schedule) {
        try {
            Schedule saved = scheduleService.saveWithHeatCheck(schedule);
            return Result.success("新增成功", saved);
        } catch (Exception e) {
            log.error("新增排班失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新排班")
    public Result<Schedule> update(@PathVariable Long id, @RequestBody Schedule schedule) {
        try {
            Schedule updated = scheduleService.updateWithHeatCheck(id, schedule);
            return Result.success("更新成功", updated);
        } catch (Exception e) {
            log.error("更新排班失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除排班")
    public Result<?> delete(@PathVariable Long id) {
        try {
            scheduleService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除排班失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询排班")
    public Result<Schedule> getById(@PathVariable Long id) {
        Schedule schedule = scheduleService.getById(id);
        if (schedule == null) {
            return Result.error("排班不存在");
        }
        return Result.success(schedule);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询排班")
    public Result<PageResult<Schedule>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String scheduleDate,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) String positionType,
            @RequestParam(required = false) String scheduleStatus) {
        Page<Schedule> page = scheduleService.pageQuery(current, size, scheduleDate, workerId, positionType, scheduleStatus);
        PageResult<Schedule> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
}
