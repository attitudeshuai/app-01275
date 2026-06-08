package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_category")
public class BizCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String categoryName;
    private Long parentId;
    private Integer sort;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<BizCategory> children;
}
