package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_supplier")
public class BizSupplier {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private String bankName;
    private String bankAccount;
    private String remark;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
