package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysMenu;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
