package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.HeatSupply;
import com.scaffolding.service.HeatSupplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/heat-supply")
@Api(tags = "防暑物资管理")
public class HeatSupplyController {

    @Autowired
    private HeatSupplyService heatSupplyService;

    @PostMapping
    @ApiOperation("新增防暑物资")
    public Result<HeatSupply> save(@RequestBody HeatSupply supply) {
        try {
            supply.setCreateTime(LocalDateTime.now());
            supply.setUpdateTime(LocalDateTime.now());
            heatSupplyService.save(supply);
            return Result.success("新增成功", supply);
        } catch (Exception e) {
            log.error("新增防暑物资失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新防暑物资")
    public Result<HeatSupply> update(@PathVariable Long id, @RequestBody HeatSupply supply) {
        try {
            supply.setId(id);
            supply.setUpdateTime(LocalDateTime.now());
            heatSupplyService.updateById(supply);
            return Result.success("更新成功", supply);
        } catch (Exception e) {
            log.error("更新防暑物资失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除防暑物资")
    public Result<?> delete(@PathVariable Long id) {
        try {
            heatSupplyService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除防暑物资失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询防暑物资")
    public Result<HeatSupply> getById(@PathVariable Long id) {
        HeatSupply supply = heatSupplyService.getById(id);
        if (supply == null) {
            return Result.error("物资不存在");
        }
        return Result.success(supply);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有防暑物资列表")
    public Result<List<HeatSupply>> list() {
        return Result.success(heatSupplyService.list());
    }

    @GetMapping("/page")
    @ApiOperation("分页查询防暑物资")
    public Result<PageResult<HeatSupply>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String supplyName,
            @RequestParam(required = false) String supplyCode) {
        Page<HeatSupply> page = heatSupplyService.pageQuery(current, size, supplyName, supplyCode);
        PageResult<HeatSupply> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
}
