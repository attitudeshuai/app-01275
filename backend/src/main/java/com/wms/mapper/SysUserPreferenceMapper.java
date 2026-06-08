package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysUserPreference;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserPreferenceMapper extends BaseMapper<SysUserPreference> {
    
    @Select("SELECT * FROM sys_user_preference WHERE user_id = #{userId} AND pref_key = #{prefKey}")
    SysUserPreference selectByUserIdAndKey(@Param("userId") Long userId, @Param("prefKey") String prefKey);
}
