package com.wms.service;

import com.wms.mapper.SysCaptchaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaptchaCleanupTask {

    private final SysCaptchaMapper captchaMapper;

    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void cleanExpiredCaptcha() {
        int count = captchaMapper.deleteExpired();
        if (count > 0) {
            log.info("清理过期验证码: {} 条", count);
        }
    }
}
