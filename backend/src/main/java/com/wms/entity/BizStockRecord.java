package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_stock_record")
public class BizStockRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long warehouseId;
    private String recordType; // IN入库 OUT出库
    private Integer quantity;
    private Integer beforeQty;
    private Integer afterQty;
    private String refType;
    private Long refId;
    private String refNo;
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String goodsName;
    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String operatorName;
}
