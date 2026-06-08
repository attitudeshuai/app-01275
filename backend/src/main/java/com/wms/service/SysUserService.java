package com.wms.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.SysUser;
import com.wms.entity.SysUserRole;
import com.wms.mapper.SysUserMapper;
import com.wms.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Value("${security.default-password:123456}")
    private String defaultPassword;

    public PageResult<SysUser> page(int pageNum, int pageSize, String username, String realName, Long deptId) {
        IPage<SysUser> page = userMapper.selectUserPage(new Page<>(pageNum, pageSize), username, realName, deptId);
        for (SysUser user : page.getRecords()) {
            user.setRoleIds(userMapper.selectRoleIdsByUserId(user.getId()));
        }
        return PageResult.of(page);
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectUserById(id);
        if (user != null) {
            user.setRoleIds(userMapper.selectRoleIdsByUserId(id));
        }
        return user;
    }

    @Transactional
    public void save(SysUser user) {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(defaultPassword));
        userMapper.insert(user);
        saveUserRoles(user.getId(), user.getRoleIds());
        log.info("新增用户: {}", user.getUsername());
    }

    @Transactional
    public void update(SysUser user) {
        SysUser existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        if (!existing.getUsername().equals(user.getUsername()) &&
            userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(null);
        userMapper.updateById(user);
        saveUserRoles(user.getId(), user.getRoleIds());
        log.info("更新用户: {}", user.getUsername());
    }

    @Transactional
    public void delete(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        log.info("删除用户: {}", id);
    }

    public void updateStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public void resetPassword(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(BCrypt.hashpw(defaultPassword));
        userMapper.updateById(user);
        log.info("重置用户密码: {}", id);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    /**
     * 更新当前用户个人信息
     */
    public void updateUserInfo(SysUser user) {
        Long currentUserId = com.wms.util.UserContext.getUserId();
        SysUser existing = userMapper.selectById(currentUserId);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        // 只允许更新部分字段
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUserId);
        updateUser.setRealName(user.getRealName());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        userMapper.updateById(updateUser);
        log.info("用户更新个人信息: {}", currentUserId);
    }
}
