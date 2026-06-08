package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizStockCheck;
import org.apache.ibatis.annotations.Param;

public interface BizStockCheckMapper extends BaseMapper<BizStockCheck> {
    
    IPage<BizStockCheck> selectCheckPage(IPage<BizStockCheck> page, @Param("checkNo") String checkNo,
                                          @Param("warehouseId") Long warehouseId,
                                          @Param("status") Integer status);
    
    BizStockCheck selectCheckById(@Param("id") Long id);
}
