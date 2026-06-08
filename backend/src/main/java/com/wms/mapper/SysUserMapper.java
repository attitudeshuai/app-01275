package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {
    
    IPage<SysUser> selectUserPage(IPage<SysUser> page, @Param("username") String username, 
                                   @Param("realName") String realName, @Param("deptId") Long deptId);
    
    SysUser selectUserById(@Param("id") Long id);
    
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
