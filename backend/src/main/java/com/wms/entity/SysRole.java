package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String roleCode;
    private String remark;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<Long> menuIds;
}
