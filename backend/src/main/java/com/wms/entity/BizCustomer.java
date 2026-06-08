package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_customer")
public class BizCustomer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerCode;
    private String customerName;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private BigDecimal creditLimit;
    private String remark;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
