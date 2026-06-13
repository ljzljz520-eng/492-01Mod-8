package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.HeatConfig;
import com.scaffolding.mapper.HeatConfigMapper;
import com.scaffolding.service.HeatConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HeatConfigServiceImpl extends ServiceImpl<HeatConfigMapper, HeatConfig> implements HeatConfigService {

    @Override
    public Map<String, String> getAllConfigs() {
        Map<String, String> map = new HashMap<>();
        List<HeatConfig> list = this.list();
        for (HeatConfig c : list) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }

    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<HeatConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeatConfig::getConfigKey, configKey);
        HeatConfig config = this.getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public List<HeatConfig> listAll() {
        return this.list();
    }
}
