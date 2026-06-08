package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysCaptcha;
import org.apache.ibatis.annotations.Delete;

public interface SysCaptchaMapper extends BaseMapper<SysCaptcha> {
    
    @Delete("DELETE FROM sys_captcha WHERE expire_time < NOW()")
    int deleteExpired();
}
