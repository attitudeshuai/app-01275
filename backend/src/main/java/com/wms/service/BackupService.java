package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.SysBackup;
import com.wms.mapper.SysBackupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据备份服务
 * 提供数据库备份、恢复和定时备份功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    private final SysBackupMapper backupMapper;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.path:./backups}")
    private String backupPath;

    @Value("${backup.keep-days:30}")
    private int keepDays;

    /**
     * 手动执行数据库备份
     */
    public SysBackup backup(String remark) {
        String dbName = extractDbName(dbUrl);
        String dbHost = extractDbHost(dbUrl);
        String dbPort = extractDbPort(dbUrl);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("wms_backup_%s.sql", timestamp);
        
        // 确保备份目录存在
        Path backupDir = Paths.get(backupPath);
        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            throw new BusinessException("创建备份目录失败: " + e.getMessage());
        }
        
        String filePath = backupDir.resolve(fileName).toString();
        
        // 执行mysqldump命令
        String command = String.format(
            "mysqldump -h%s -P%s -u%s -p%s %s --single-transaction --routines --triggers",
            dbHost, dbPort, dbUsername, dbPassword, dbName
        );
        
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command + " > " + filePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // 读取错误信息
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                throw new BusinessException("备份失败: " + error);
            }
            
            // 获取文件大小
            File backupFile = new File(filePath);
            long fileSize = backupFile.length();
            
            // 保存备份记录
            SysBackup backup = new SysBackup();
            backup.setFileName(fileName);
            backup.setFilePath(filePath);
            backup.setFileSize(fileSize);
            backup.setBackupType("MANUAL"); // 手动备份
            backup.setStatus(1); // 1-成功
            backup.setRemark(remark);
            backup.setOperatorId(com.wms.util.UserContext.getUserId());
            backup.setCreateTime(LocalDateTime.now());
            backupMapper.insert(backup);
            
            log.info("数据库备份成功: {}, 大小: {} bytes", fileName, fileSize);
            return backup;
            
        } catch (IOException | InterruptedException e) {
            log.error("数据库备份失败", e);
            throw new BusinessException("备份失败: " + e.getMessage());
        }
    }

    /**
     * 恢复数据库
     */
    public void restore(Long backupId) {
        SysBackup backup = backupMapper.selectById(backupId);
        if (backup == null) {
            throw new BusinessException("备份记录不存在");
        }
        
        File backupFile = new File(backup.getFilePath());
        if (!backupFile.exists()) {
            throw new BusinessException("备份文件不存在");
        }
        
        String dbName = extractDbName(dbUrl);
        String dbHost = extractDbHost(dbUrl);
        String dbPort = extractDbPort(dbUrl);
        
        String command = String.format(
            "mysql -h%s -P%s -u%s -p%s %s < %s",
            dbHost, dbPort, dbUsername, dbPassword, dbName, backup.getFilePath()
        );
        
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException("恢复失败");
            }
            
            log.info("数据库恢复成功: {}", backup.getFileName());
            
        } catch (IOException | InterruptedException e) {
            log.error("数据库恢复失败", e);
            throw new BusinessException("恢复失败: " + e.getMessage());
        }
    }

    /**
     * 删除备份
     */
    public void delete(Long backupId) {
        SysBackup backup = backupMapper.selectById(backupId);
        if (backup == null) {
            throw new BusinessException("备份记录不存在");
        }
        
        // 删除文件
        try {
            Files.deleteIfExists(Paths.get(backup.getFilePath()));
        } catch (IOException e) {
            log.warn("删除备份文件失败: {}", e.getMessage());
        }
        
        // 删除记录
        backupMapper.deleteById(backupId);
        log.info("删除备份: {}", backup.getFileName());
    }

    /**
     * 分页查询备份列表
     */
    public PageResult<SysBackup> page(int pageNum, int pageSize) {
        long total = backupMapper.selectCount(null);
        List<SysBackup> records = backupMapper.selectList(
            new LambdaQueryWrapper<SysBackup>()
                .orderByDesc(SysBackup::getCreateTime)
                .last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize)
        );
        return new PageResult<>(records, total);
    }

    /**
     * 定时备份 - 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackup() {
        log.info("开始执行定时备份...");
        try {
            String dbName = extractDbName(dbUrl);
            String dbHost = extractDbHost(dbUrl);
            String dbPort = extractDbPort(dbUrl);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("wms_auto_backup_%s.sql", timestamp);
            
            Path backupDir = Paths.get(backupPath);
            Files.createDirectories(backupDir);
            String filePath = backupDir.resolve(fileName).toString();
            
            String command = String.format(
                "mysqldump -h%s -P%s -u%s -p%s %s --single-transaction --routines --triggers",
                dbHost, dbPort, dbUsername, dbPassword, dbName
            );
            
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command + " > " + filePath);
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                File backupFile = new File(filePath);
                SysBackup backup = new SysBackup();
                backup.setFileName(fileName);
                backup.setFilePath(filePath);
                backup.setFileSize(backupFile.length());
                backup.setBackupType("AUTO"); // 自动备份
                backup.setStatus(1);
                backup.setRemark("定时自动备份");
                backup.setCreateTime(LocalDateTime.now());
                backupMapper.insert(backup);
                log.info("定时备份成功: {}", fileName);
            } else {
                log.error("定时备份失败");
            }
            
            // 清理过期备份
            cleanExpiredBackups();
            
        } catch (Exception e) {
            log.error("定时备份异常", e);
        }
    }

    /**
     * 清理过期备份
     */
    private void cleanExpiredBackups() {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(keepDays);
        List<SysBackup> expiredBackups = backupMapper.selectList(
            new LambdaQueryWrapper<SysBackup>()
                .lt(SysBackup::getCreateTime, expireTime)
                .eq(SysBackup::getBackupType, "AUTO") // 只清理自动备份
        );
        
        for (SysBackup backup : expiredBackups) {
            try {
                Files.deleteIfExists(Paths.get(backup.getFilePath()));
                backupMapper.deleteById(backup.getId());
                log.info("清理过期备份: {}", backup.getFileName());
            } catch (IOException e) {
                log.warn("清理备份文件失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 下载备份文件
     */
    public void download(Long backupId, jakarta.servlet.http.HttpServletResponse response) {
        SysBackup backup = backupMapper.selectById(backupId);
        if (backup == null) {
            throw new BusinessException("备份记录不存在");
        }
        
        File backupFile = new File(backup.getFilePath());
        if (!backupFile.exists()) {
            throw new BusinessException("备份文件不存在");
        }
        
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                java.net.URLEncoder.encode(backup.getFileName(), "UTF-8"));
            response.setContentLengthLong(backupFile.length());
            
            try (InputStream is = new FileInputStream(backupFile);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            log.info("下载备份文件: {}", backup.getFileName());
        } catch (IOException e) {
            log.error("下载备份文件失败", e);
            throw new BusinessException("下载失败: " + e.getMessage());
        }
    }

    private String extractDbName(String url) {
        // jdbc:mysql://localhost:3306/wms?...
        String path = url.split("\\?")[0];
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private String extractDbHost(String url) {
        // jdbc:mysql://localhost:3306/wms
        String hostPort = url.split("//")[1].split("/")[0];
        return hostPort.split(":")[0];
    }

    private String extractDbPort(String url) {
        String hostPort = url.split("//")[1].split("/")[0];
        return hostPort.contains(":") ? hostPort.split(":")[1] : "3306";
    }
}
