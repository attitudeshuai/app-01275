package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.annotation.RequirePermission;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.SysLog;
import com.wms.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService logService;

    @RequirePermission("sys:log:list")
    @GetMapping("/page")
    public Result<PageResult<SysLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username) {
        return Result.success(logService.page(page, size, module, username));
    }

    @RequirePermission("sys:log:clear")
    @Log(module = "日志管理", operation = "清空日志")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        logService.clear();
        return Result.success();
    }
}
