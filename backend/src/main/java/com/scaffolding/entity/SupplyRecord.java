package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("supply_record")
public class SupplyRecord extends BaseEntity {

    private String recordNo;

    private Long supplyId;

    private String supplyName;

    private String supplyCode;

    private String unit;

    private Integer quantity;

    private Long workerId;

    private String workerName;

    private Long scheduleId;

    private Long warehouseId;

    private String warehouseName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime issueTime;

    private String issueStatus;

    private String remark;
}
