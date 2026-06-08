package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统通知/预警消息实体
 */
@Data
@TableName("sys_notification")
public class SysNotification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;           // 通知标题
    private String content;         // 通知内容
    private String type;            // 类型: STOCK_WARNING库存预警, SYSTEM系统通知
    private Long userId;            // 接收用户ID，null表示所有用户
    private Integer status;         // 状态: 0未读 1已读
    private Long refId;             // 关联ID（如商品ID）
    private String refType;         // 关联类型
    private LocalDateTime createTime;
    private LocalDateTime readTime;
}
