package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统备份记录实体
 */
@Data
@TableName("sys_backup")
public class SysBackup {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 备份文件名 */
    @TableField("backup_name")
    private String fileName;
    
    /** 备份文件路径 */
    private String filePath;
    
    /** 文件大小(字节) */
    private Long fileSize;
    
    /** 备份类型: MANUAL-手动备份, AUTO-自动备份 */
    private String backupType;
    
    /** 状态: 0-失败, 1-成功 */
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    /** 操作人ID */
    private Long operatorId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
