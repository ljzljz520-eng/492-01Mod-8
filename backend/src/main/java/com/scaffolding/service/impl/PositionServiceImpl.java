package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Position;
import com.scaffolding.mapper.PositionMapper;
import com.scaffolding.service.PositionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {

    @Override
    public Page<Position> pageQuery(Long current, Long size, String positionName, String positionType) {
        Page<Position> page = new Page<>(current, size);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(positionName)) {
            wrapper.like(Position::getPositionName, positionName);
        }
        if (StringUtils.hasText(positionType)) {
            wrapper.eq(Position::getPositionType, positionType);
        }

        wrapper.orderByDesc(Position::getCreateTime);
        return this.page(page, wrapper);
    }
}
