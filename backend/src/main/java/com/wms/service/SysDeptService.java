package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.BusinessException;
import com.wms.entity.SysDept;
import com.wms.mapper.SysDeptMapper;
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
public class SysDeptService {

    private final SysDeptMapper deptMapper;

    public List<SysDept> tree() {
        List<SysDept> depts = deptMapper.selectList(
            new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getSort));
        return buildTree(depts);
    }

    public void save(SysDept dept) {
        deptMapper.insert(dept);
        log.info("新增部门: {}", dept.getDeptName());
    }

    public void update(SysDept dept) {
        deptMapper.updateById(dept);
        log.info("更新部门: {}", dept.getDeptName());
    }

    public void delete(Long id) {
        long count = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (count > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }
        deptMapper.deleteById(id);
        log.info("删除部门: {}", id);
    }

    private List<SysDept> buildTree(List<SysDept> depts) {
        Map<Long, SysDept> deptMap = depts.stream().collect(Collectors.toMap(SysDept::getId, d -> d));
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : depts) {
            if (dept.getParentId() == 0) {
                tree.add(dept);
            } else {
                SysDept parent = deptMap.get(dept.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dept);
                }
            }
        }
        return tree;
    }
}
