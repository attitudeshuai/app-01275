package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizStockAdjust;
import org.apache.ibatis.annotations.Param;

public interface BizStockAdjustMapper extends BaseMapper<BizStockAdjust> {
    
    IPage<BizStockAdjust> selectAdjustPage(IPage<BizStockAdjust> page, @Param("adjustType") String adjustType,
                                            @Param("warehouseId") Long warehouseId);
}
