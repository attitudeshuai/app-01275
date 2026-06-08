package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.BusinessException;
import com.wms.entity.SysMenu;
import com.wms.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;

    public List<SysMenu> tree() {
        List<SysMenu> menus = menuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        return buildTree(menus);
    }

    public void save(SysMenu menu) {
        menuMapper.insert(menu);
        log.info("新增菜单: {}", menu.getMenuName());
    }

    public void update(SysMenu menu) {
        menuMapper.updateById(menu);
        log.info("更新菜单: {}", menu.getMenuName());
    }

    public void delete(Long id) {
        long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (count > 0) {
            throw new BusinessException("存在子菜单，无法删除");
        }
        menuMapper.deleteById(id);
        log.info("删除菜单: {}", id);
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream().collect(Collectors.toMap(SysMenu::getId, m -> m));
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == 0) {
                tree.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        return tree;
    }
}
