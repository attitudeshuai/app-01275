package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_captcha")
public class SysCaptcha {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String uuid;
    private String code;
    private LocalDateTime expireTime;
}
