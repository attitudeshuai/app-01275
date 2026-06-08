package com.wms.service;

import com.wms.entity.BizStock;
import com.wms.mapper.*;
import com.wms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BizSaleOrderMapper saleOrderMapper;
    private final BizSaleItemMapper saleItemMapper;
    private final BizPurchaseOrderMapper purchaseOrderMapper;
    private final BizPurchaseItemMapper purchaseItemMapper;
    private final BizStockMapper stockMapper;
    private final BizGoodsMapper goodsMapper;

    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        
        // 今日采购额
        BigDecimal todayPurchase = purchaseOrderMapper.sumAmountByDateRange(todayStart, todayEnd);
        vo.setTodayPurchase(todayPurchase != null ? todayPurchase : BigDecimal.ZERO);
        
        // 本月采购额
        BigDecimal monthPurchase = purchaseOrderMapper.sumAmountByDateRange(monthStart, todayEnd);
        vo.setMonthPurchase(monthPurchase != null ? monthPurchase : BigDecimal.ZERO);
        
        // 今日销售额
        BigDecimal todaySales = saleOrderMapper.sumAmountByDateRange(todayStart, todayEnd);
        vo.setTodaySales(todaySales != null ? todaySales : BigDecimal.ZERO);
        
        // 本月销售额
        BigDecimal monthSales = saleOrderMapper.sumAmountByDateRange(monthStart, todayEnd);
        vo.setMonthSales(monthSales != null ? monthSales : BigDecimal.ZERO);
        
        // 库存预警数
        List<BizStock> warnings = stockMapper.selectWarningList();
        vo.setWarningCount(warnings != null ? warnings.size() : 0);
        
        // 待收款金额
        BigDecimal pendingReceive = saleOrderMapper.sumPendingReceive();
        vo.setPendingReceive(pendingReceive != null ? pendingReceive : BigDecimal.ZERO);
        
        // 待审批采购单数
        Integer pendingPurchase = purchaseOrderMapper.countByStatus(0);
        vo.setPendingOrders(pendingPurchase != null ? pendingPurchase : 0);
        
        // 近7天销售趋势
        List<Map<String, Object>> salesTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            BigDecimal amount = saleOrderMapper.sumAmountByDateRange(
                date.atStartOfDay(), 
                date.plusDays(1).atStartOfDay()
            );
            item.put("sales", amount != null ? amount : BigDecimal.ZERO);
            BigDecimal purchaseAmount = purchaseOrderMapper.sumAmountByDateRange(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
            );
            item.put("purchase", purchaseAmount != null ? purchaseAmount : BigDecimal.ZERO);
            salesTrend.add(item);
        }
        vo.setSalesTrend(salesTrend);
        
        // 商品分类销售占比
        List<Map<String, Object>> categoryRatio = saleItemMapper.selectCategorySalesRatio();
        if (categoryRatio == null || categoryRatio.isEmpty()) {
            categoryRatio = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("name", "暂无数据");
            item.put("value", 100);
            categoryRatio.add(item);
        }
        vo.setCategoryRatio(categoryRatio);
        
        // 待审批采购单列表(最多5条)
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.wms.entity.BizPurchaseOrder> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 5);
        vo.setPendingPurchases(purchaseOrderMapper.selectOrderPage(page, null, null, 0, null).getRecords());
        
        // 库存预警列表(最多5条)
        List<BizStock> allWarnings = stockMapper.selectWarningList();
        vo.setStockWarnings(allWarnings != null && allWarnings.size() > 5 ? allWarnings.subList(0, 5) : allWarnings);
        
        return vo;
    }

    public List<SaleRankVO> getSaleRank(String startDate, String endDate, int limit) {
        return saleItemMapper.selectSaleRank(startDate, endDate, limit);
    }

    public List<Map<String, Object>> getPurchaseTrend(String startDate, String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        while (!start.isAfter(end)) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", start.toString());
            BigDecimal amount = purchaseOrderMapper.sumAmountByDateRange(
                start.atStartOfDay(),
                start.plusDays(1).atStartOfDay()
            );
            item.put("totalAmount", amount != null ? amount : BigDecimal.ZERO);
            Integer count = purchaseOrderMapper.countByDateRange(
                start.atStartOfDay(),
                start.plusDays(1).atStartOfDay()
            );
            item.put("orderCount", count != null ? count : 0);
            list.add(item);
            start = start.plusDays(1);
        }
        return list;
    }

    public List<StockWarningVO> getStockWarningReport() {
        List<BizStock> warnings = stockMapper.selectWarningList();
        List<StockWarningVO> list = new ArrayList<>();
        if (warnings != null) {
            for (BizStock stock : warnings) {
                StockWarningVO vo = new StockWarningVO();
                vo.setGoodsCode(stock.getGoodsCode());
                vo.setGoodsName(stock.getGoodsName());
                vo.setWarehouseName(stock.getWarehouseName());
                vo.setQuantity(stock.getQuantity());
                vo.setSafetyStock(stock.getSafetyStock());
                vo.setMaxStock(stock.getMaxStock());
                vo.setWarningType(stock.getQuantity() < stock.getSafetyStock() ? "低于安全库存" : "超过最大库存");
                list.add(vo);
            }
        }
        return list;
    }
    
    public Map<String, Object> getStockWarningReportWithStats() {
        List<BizStock> allStocks = stockMapper.selectAllWithGoods();
        List<StockWarningVO> warnings = new ArrayList<>();
        int lowCount = 0;
        int highCount = 0;
        int normalCount = 0;
        
        if (allStocks != null) {
            for (BizStock stock : allStocks) {
                boolean isLow = stock.getQuantity() < stock.getSafetyStock();
                boolean isHigh = stock.getQuantity() > stock.getMaxStock();
                
                if (isLow || isHigh) {
                    StockWarningVO vo = new StockWarningVO();
                    vo.setGoodsCode(stock.getGoodsCode());
                    vo.setGoodsName(stock.getGoodsName());
                    vo.setWarehouseName(stock.getWarehouseName());
                    vo.setQuantity(stock.getQuantity());
                    vo.setSafetyStock(stock.getSafetyStock());
                    vo.setMaxStock(stock.getMaxStock());
                    vo.setWarningType(isLow ? "低于安全库存" : "超过最大库存");
                    warnings.add(vo);
                }
                
                if (isLow) lowCount++;
                else if (isHigh) highCount++;
                else normalCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", warnings);
        result.put("total", warnings.size());
        Map<String, Integer> stats = new HashMap<>();
        stats.put("lowCount", lowCount);
        stats.put("highCount", highCount);
        stats.put("normalCount", normalCount);
        result.put("stats", stats);
        return result;
    }

    public List<ProfitVO> getProfitAnalysis(String startDate, String endDate) {
        return saleItemMapper.selectProfitAnalysis(startDate, endDate);
    }
    
    public Map<String, Object> getProfitReport(String startDate, String endDate) {
        List<ProfitVO> list = saleItemMapper.selectProfitAnalysis(startDate, endDate);
        
        BigDecimal totalSale = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        if (list != null) {
            for (ProfitVO vo : list) {
                totalSale = totalSale.add(vo.getSaleAmount() != null ? vo.getSaleAmount() : BigDecimal.ZERO);
                totalCost = totalCost.add(vo.getCostAmount() != null ? vo.getCostAmount() : BigDecimal.ZERO);
            }
        }
        
        BigDecimal totalProfit = totalSale.subtract(totalCost);
        BigDecimal profitRate = totalSale.compareTo(BigDecimal.ZERO) > 0 
            ? totalProfit.multiply(new BigDecimal("100")).divide(totalSale, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        // 生成趋势数据
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        while (!start.isAfter(end)) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", start.toString());
            // 使用发货时间计算销售额（已发货的订单）
            BigDecimal daySale = saleItemMapper.sumSaleByShipDate(start.toString());
            BigDecimal dayCost = saleItemMapper.sumCostByDateRange(start.toString(), start.toString());
            item.put("saleAmount", daySale != null ? daySale : BigDecimal.ZERO);
            item.put("costAmount", dayCost != null ? dayCost : BigDecimal.ZERO);
            item.put("profit", (daySale != null ? daySale : BigDecimal.ZERO).subtract(dayCost != null ? dayCost : BigDecimal.ZERO));
            trend.add(item);
            start = start.plusDays(1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", list != null ? list : new ArrayList<>());
        result.put("total", list != null ? list.size() : 0);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSale", totalSale);
        stats.put("totalCost", totalCost);
        stats.put("totalProfit", totalProfit);
        stats.put("profitRate", profitRate);
        result.put("stats", stats);
        result.put("trend", trend);
        return result;
    }
}
