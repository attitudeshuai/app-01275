package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.BizSaleOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BizSaleOrderMapper extends BaseMapper<BizSaleOrder> {
    
    IPage<BizSaleOrder> selectOrderPage(IPage<BizSaleOrder> page, @Param("orderNo") String orderNo,
                                         @Param("customerId") Long customerId, @Param("status") Integer status,
                                         @Param("deptId") Long deptId);
    
    BizSaleOrder selectOrderById(@Param("id") Long id);
    
    BigDecimal sumAmountByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    BigDecimal sumPendingReceive();
}
