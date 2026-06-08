package com.wms.vo;

import com.wms.entity.BizPurchaseOrder;
import com.wms.entity.BizStock;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private BigDecimal todayPurchase;
    private BigDecimal monthPurchase;
    private BigDecimal todaySales;
    private BigDecimal monthSales;
    private Integer pendingOrders;
    private Integer warningCount;
    private BigDecimal pendingReceive;
    private List<Map<String, Object>> salesTrend;
    private List<Map<String, Object>> categoryRatio;
    private List<BizPurchaseOrder> pendingPurchases;
    private List<BizStock> stockWarnings;
}
