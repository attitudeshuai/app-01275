package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizPurchaseOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BizPurchaseOrderMapper extends BaseMapper<BizPurchaseOrder> {
    
    IPage<BizPurchaseOrder> selectOrderPage(IPage<BizPurchaseOrder> page, @Param("orderNo") String orderNo,
                                             @Param("supplierId") Long supplierId, @Param("status") Integer status,
                                             @Param("deptId") Long deptId);
    
    BizPurchaseOrder selectOrderById(@Param("id") Long id);
    
    BigDecimal sumAmountByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    Integer countByStatus(@Param("status") Integer status);
    
    Integer countByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
