package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_warehouse")
public class BizWarehouse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String warehouseCode;
    private String warehouseName;
    private String address;
    private String manager;
    private String phone;
    private String remark;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}
