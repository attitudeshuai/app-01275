package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.entity.SysLog;
import com.wms.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysLogService {

    private final SysLogMapper logMapper;

    public PageResult<SysLog> page(int pageNum, int pageSize, String module, String username) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.like(SysLog::getModule, module);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLog::getUsername, username);
        }
        wrapper.orderByDesc(SysLog::getCreateTime);
        IPage<SysLog> page = logMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page);
    }

    public void clear() {
        logMapper.delete(null);
    }
}
