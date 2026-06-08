package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String menuName;
    private Long parentId;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer type;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<SysMenu> children;
}
