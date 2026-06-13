package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("position")
public class Position extends BaseEntity {

    private String positionName;

    private String positionType;

    private BigDecimal hourlyWage;

    private BigDecimal heatAllowance;

    private String remark;
}
