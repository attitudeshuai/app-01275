package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.BizSaleItem;
import com.wms.vo.ProfitVO;
import com.wms.vo.SaleRankVO;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BizSaleItemMapper extends BaseMapper<BizSaleItem> {
    
    List<BizSaleItem> selectByOrderId(@Param("orderId") Long orderId);
    
    List<Map<String, Object>> selectCategorySalesRatio();
    
    List<SaleRankVO> selectSaleRank(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") int limit);
    
    List<ProfitVO> selectProfitAnalysis(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    BigDecimal sumCostByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    
    BigDecimal sumSaleByShipDate(@Param("shipDate") String shipDate);
}
