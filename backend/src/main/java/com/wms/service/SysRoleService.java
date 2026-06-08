package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.BusinessException;
import com.wms.common.PageResult;
import com.wms.entity.SysRole;
import com.wms.entity.SysRoleMenu;
import com.wms.mapper.SysRoleMapper;
import com.wms.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public PageResult<SysRole> page(int pageNum, int pageSize, String roleName) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        IPage<SysRole> page = roleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page);
    }

    public List<SysRole> list() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1));
    }

    public SysRole getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role != null) {
            role.setMenuIds(roleMapper.selectMenuIdsByRoleId(id));
        }
        return role;
    }

    @Transactional
    public void save(SysRole role) {
        if (roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, role.getRoleCode())) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        roleMapper.insert(role);
        log.info("新增角色: {}", role.getRoleName());
    }

    @Transactional
    public void update(SysRole role) {
        SysRole existing = roleMapper.selectById(role.getId());
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        roleMapper.updateById(role);
        log.info("更新角色: {}", role.getRoleName());
    }

    @Transactional
    public void delete(Long id) {
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        log.info("删除角色: {}", id);
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        log.info("分配角色菜单: roleId={}, menuIds={}", roleId, menuIds);
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }
}
