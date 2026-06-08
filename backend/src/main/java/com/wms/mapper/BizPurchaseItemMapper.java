package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.BizPurchaseItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BizPurchaseItemMapper extends BaseMapper<BizPurchaseItem> {
    
    List<BizPurchaseItem> selectByOrderId(@Param("orderId") Long orderId);
}
