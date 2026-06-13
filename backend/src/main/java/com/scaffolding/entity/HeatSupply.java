package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("heat_supply")
public class HeatSupply extends BaseEntity {

    private String supplyName;

    private String supplyCode;

    private String supplyType;

    private String unit;

    private Integer stockQuantity;

    private String description;
}
