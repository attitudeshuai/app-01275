package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.BizStockCheckItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BizStockCheckItemMapper extends BaseMapper<BizStockCheckItem> {
    
    List<BizStockCheckItem> selectByCheckId(@Param("checkId") Long checkId);
}
