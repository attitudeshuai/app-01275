package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizStockRecord;
import org.apache.ibatis.annotations.Param;

public interface BizStockRecordMapper extends BaseMapper<BizStockRecord> {
    
    IPage<BizStockRecord> selectRecordPage(IPage<BizStockRecord> page, @Param("goodsId") Long goodsId,
                                            @Param("warehouseId") Long warehouseId, @Param("recordType") String recordType);
}
