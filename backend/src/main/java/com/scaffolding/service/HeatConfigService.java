package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.HeatConfig;

import java.util.List;
import java.util.Map;

public interface HeatConfigService extends IService<HeatConfig> {
    Map<String, String> getAllConfigs();

    String getConfigValue(String configKey);

    List<HeatConfig> listAll();
}
