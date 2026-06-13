package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_hour")
public class WorkHour extends BaseEntity {

    private String settleMonth;

    private Long workerId;

    private String workerName;

    private BigDecimal normalHours;

    private BigDecimal normalWage;

    private Integer heatDays;

    private BigDecimal heatHours;

    private BigDecimal heatAllowanceTotal;

    private BigDecimal totalWage;

    private String settleStatus;

    private String remark;
}
