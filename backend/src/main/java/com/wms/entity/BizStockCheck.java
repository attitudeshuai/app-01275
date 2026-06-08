package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_stock_check")
public class BizStockCheck {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String checkNo;
    private Long warehouseId;
    private Integer status; // 0进行中 1已完成
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;

    @TableField(exist = false)
    private String warehouseName;
    @TableField(exist = false)
    private String operatorName;
    @TableField(exist = false)
    private List<BizStockCheckItem> items;
}
