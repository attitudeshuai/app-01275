package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Long execTime;
    private LocalDateTime createTime;
}
