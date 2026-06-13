package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("heat_config")
public class HeatConfig extends BaseEntity {

    private String configKey;

    private String configValue;

    private String configName;

    private String description;
}
