package com.scaffolding.controller;

import com.scaffolding.common.Result;
import com.scaffolding.entity.HeatConfig;
import com.scaffolding.service.HeatConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/heat-config")
@Api(tags = "高温配置")
public class HeatConfigController {

    @Autowired
    private HeatConfigService heatConfigService;

    @GetMapping("/all")
    @ApiOperation("获取所有高温配置（Map形式）")
    public Result<Map<String, String>> getAllConfigs() {
        return Result.success(heatConfigService.getAllConfigs());
    }

    @GetMapping("/list")
    @ApiOperation("获取所有高温配置（列表形式）")
    public Result<List<HeatConfig>> list() {
        return Result.success(heatConfigService.listAll());
    }

    @GetMapping("/{key}")
    @ApiOperation("根据配置键获取值")
    public Result<String> getByKey(@PathVariable String key) {
        String value = heatConfigService.getConfigValue(key);
        if (value == null) {
            return Result.error("配置不存在");
        }
        return Result.success(value);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新高温配置")
    public Result<HeatConfig> update(@PathVariable Long id, @RequestBody HeatConfig config) {
        try {
            config.setId(id);
            config.setUpdateTime(LocalDateTime.now());
            heatConfigService.updateById(config);
            return Result.success("更新成功", config);
        } catch (Exception e) {
            log.error("更新高温配置失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
