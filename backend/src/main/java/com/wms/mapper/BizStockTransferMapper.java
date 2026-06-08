package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizStockTransfer;
import org.apache.ibatis.annotations.Param;

public interface BizStockTransferMapper extends BaseMapper<BizStockTransfer> {
    
    IPage<BizStockTransfer> selectTransferPage(IPage<BizStockTransfer> page, @Param("goodsId") Long goodsId);
}
