package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_dept")
public class SysDept {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deptName;
    private Long parentId;
    private Integer sort;
    private String leader;
    private String phone;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<SysDept> children;
}
