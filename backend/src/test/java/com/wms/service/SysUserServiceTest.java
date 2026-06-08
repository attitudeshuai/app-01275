package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.SysUser;
import com.wms.mapper.SysUserMapper;
import com.wms.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysUserServiceTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private SysUserService sysUserService;

    @Test
    void page_ShouldReturnPageResult() {
        Page<SysUser> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(userMapper.selectUserPage(any(IPage.class), isNull(), isNull(), isNull())).thenReturn(page);

        PageResult<SysUser> result = sysUserService.page(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void page_WithUsername_ShouldFilterByUsername() {
        Page<SysUser> page = new Page<>(1, 10);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        page.setRecords(Arrays.asList(user));
        page.setTotal(1);

        when(userMapper.selectUserPage(any(IPage.class), eq("admin"), isNull(), isNull())).thenReturn(page);
        when(userMapper.selectRoleIdsByUserId(1L)).thenReturn(new ArrayList<>());

        PageResult<SysUser> result = sysUserService.page(1, 10, "admin", null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    void getById_ShouldReturnUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");

        when(userMapper.selectUserById(1L)).thenReturn(user);
        when(userMapper.selectRoleIdsByUserId(1L)).thenReturn(Arrays.asList(1L, 2L));

        SysUser result = sysUserService.getById(1L);

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals(2, result.getRoleIds().size());
    }

    @Test
    void save_WithNewUser_ShouldInsert() {
        SysUser user = new SysUser();
        user.setUsername("newuser");
        user.setRoleIds(Arrays.asList(1L));

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(SysUser.class))).thenReturn(1);

        assertDoesNotThrow(() -> sysUserService.save(user));
        verify(userMapper).insert(any(SysUser.class));
    }

    @Test
    void save_WithExistingUsername_ShouldThrowException() {
        SysUser user = new SysUser();
        user.setUsername("admin");

        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> sysUserService.save(user));
    }

    @Test
    void update_WithValidUser_ShouldUpdate() {
        SysUser existing = new SysUser();
        existing.setId(1L);
        existing.setUsername("admin");

        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("管理员");

        when(userMapper.selectById(1L)).thenReturn(existing);
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        assertDoesNotThrow(() -> sysUserService.update(user));
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void delete_ShouldDeleteUserAndRoles() {
        when(userMapper.deleteById(1L)).thenReturn(1);
        when(userRoleMapper.delete(any())).thenReturn(1);

        assertDoesNotThrow(() -> sysUserService.delete(1L));
        verify(userMapper).deleteById(1L);
        verify(userRoleMapper).delete(any());
    }

    @Test
    void updateStatus_ShouldUpdateUserStatus() {
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        assertDoesNotThrow(() -> sysUserService.updateStatus(1L, 0));
        verify(userMapper).updateById(any(SysUser.class));
    }

    @Test
    void resetPassword_ShouldResetToDefault() {
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);

        assertDoesNotThrow(() -> sysUserService.resetPassword(1L));
        verify(userMapper).updateById(any(SysUser.class));
    }
}
