package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.HeatSupply;

public interface HeatSupplyService extends IService<HeatSupply> {
    Page<HeatSupply> pageQuery(Long current, Long size, String supplyName, String supplyCode);
}
