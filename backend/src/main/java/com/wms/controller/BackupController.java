package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.SysBackup;
import com.wms.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 数据备份管理Controller
 */
@RestController
@RequestMapping("/api/sys/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @RequirePermission("sys:backup:list")
    @GetMapping("/page")
    public Result<PageResult<SysBackup>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(backupService.page(page, size));
    }

    @RequirePermission("sys:backup:add")
    @Log(module = "数据备份", operation = "执行备份")
    @PostMapping
    public Result<SysBackup> backup(@RequestBody(required = false) Map<String, String> params) {
        String remark = params != null ? params.get("remark") : null;
        return Result.success(backupService.backup(remark));
    }

    @RequirePermission("sys:backup:restore")
    @Log(module = "数据备份", operation = "恢复数据")
    @PostMapping("/restore/{id}")
    public Result<Void> restore(@PathVariable Long id) {
        backupService.restore(id);
        return Result.success();
    }

    @RequirePermission("sys:backup:delete")
    @Log(module = "数据备份", operation = "删除备份")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        backupService.delete(id);
        return Result.success();
    }

    @RequirePermission("sys:backup:download")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        backupService.download(id, response);
    }
}
