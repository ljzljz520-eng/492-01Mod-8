package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Position;

public interface PositionService extends IService<Position> {
    Page<Position> pageQuery(Long current, Long size, String positionName, String positionType);
}
