package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.HeatSupply;
import com.scaffolding.mapper.HeatSupplyMapper;
import com.scaffolding.service.HeatSupplyService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HeatSupplyServiceImpl extends ServiceImpl<HeatSupplyMapper, HeatSupply> implements HeatSupplyService {

    @Override
    public Page<HeatSupply> pageQuery(Long current, Long size, String supplyName, String supplyCode) {
        Page<HeatSupply> page = new Page<>(current, size);
        LambdaQueryWrapper<HeatSupply> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(supplyName)) {
            wrapper.like(HeatSupply::getSupplyName, supplyName);
        }
        if (StringUtils.hasText(supplyCode)) {
            wrapper.like(HeatSupply::getSupplyCode, supplyCode);
        }

        wrapper.orderByDesc(HeatSupply::getCreateTime);
        return this.page(page, wrapper);
    }
}
