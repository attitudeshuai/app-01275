package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户偏好设置实体
 * 用于存储用户的个性化配置，如常用商品选择历史等
 */
@Data
@TableName("sys_user_preference")
public class SysUserPreference {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String prefKey;   // 偏好键: PURCHASE_GOODS_HISTORY, SALE_GOODS_HISTORY
    private String prefValue; // 偏好值: JSON格式存储
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
