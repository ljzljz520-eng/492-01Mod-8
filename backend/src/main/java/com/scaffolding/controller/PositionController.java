package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Position;
import com.scaffolding.service.PositionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/position")
@Api(tags = "岗位管理")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @PostMapping
    @ApiOperation("新增岗位")
    public Result<Position> save(@RequestBody Position position) {
        try {
            position.setCreateTime(LocalDateTime.now());
            position.setUpdateTime(LocalDateTime.now());
            positionService.save(position);
            return Result.success("新增成功", position);
        } catch (Exception e) {
            log.error("新增岗位失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新岗位")
    public Result<Position> update(@PathVariable Long id, @RequestBody Position position) {
        try {
            position.setId(id);
            position.setUpdateTime(LocalDateTime.now());
            positionService.updateById(position);
            return Result.success("更新成功", position);
        } catch (Exception e) {
            log.error("更新岗位失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除岗位")
    public Result<?> delete(@PathVariable Long id) {
        try {
            positionService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除岗位失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询岗位")
    public Result<Position> getById(@PathVariable Long id) {
        Position position = positionService.getById(id);
        if (position == null) {
            return Result.error("岗位不存在");
        }
        return Result.success(position);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有岗位列表")
    public Result<List<Position>> list() {
        return Result.success(positionService.list());
    }

    @GetMapping("/page")
    @ApiOperation("分页查询岗位")
    public Result<PageResult<Position>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String positionType) {
        Page<Position> page = positionService.pageQuery(current, size, positionName, positionType);
        PageResult<Position> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
}
