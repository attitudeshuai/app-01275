package com.wms.service;

import com.wms.entity.BizStock;
import com.wms.mapper.BizStockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 库存预警定时任务
 * 定时检查库存，对低于安全库存或高于最大库存的商品生成预警通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockWarningTask {

    private final BizStockMapper stockMapper;
    private final NotificationService notificationService;
    
    // 记录已发送低库存预警的商品ID，避免重复发送
    private final Set<Long> lowStockWarnedIds = new HashSet<>();
    // 记录已发送高库存预警的商品ID，避免重复发送
    private final Set<Long> highStockWarnedIds = new HashSet<>();

    /**
     * 每小时检查一次库存预警
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkStockWarning() {
        log.info("开始执行库存预警检查...");
        try {
            // 检查低于安全库存的商品
            List<BizStock> lowStockList = stockMapper.selectWarningList();
            int newLowWarnings = 0;
            Set<Long> currentLowStockIds = new HashSet<>();
            
            for (BizStock stock : lowStockList) {
                currentLowStockIds.add(stock.getGoodsId());
                if (!lowStockWarnedIds.contains(stock.getGoodsId())) {
                    notificationService.createLowStockWarningNotification(
                        stock.getGoodsName(),
                        stock.getQuantity(),
                        stock.getSafetyStock(),
                        stock.getGoodsId()
                    );
                    lowStockWarnedIds.add(stock.getGoodsId());
                    newLowWarnings++;
                }
            }
            // 清理已恢复正常的商品
            lowStockWarnedIds.retainAll(currentLowStockIds);
            
            // 检查高于最大库存的商品
            List<BizStock> highStockList = stockMapper.selectOverflowList();
            int newHighWarnings = 0;
            Set<Long> currentHighStockIds = new HashSet<>();
            
            for (BizStock stock : highStockList) {
                currentHighStockIds.add(stock.getGoodsId());
                if (!highStockWarnedIds.contains(stock.getGoodsId())) {
                    notificationService.createHighStockWarningNotification(
                        stock.getGoodsName(),
                        stock.getQuantity(),
                        stock.getMaxStock(),
                        stock.getGoodsId()
                    );
                    highStockWarnedIds.add(stock.getGoodsId());
                    newHighWarnings++;
                }
            }
            // 清理已恢复正常的商品
            highStockWarnedIds.retainAll(currentHighStockIds);
            
            log.info("库存预警检查完成，新增低库存预警: {} 条，新增高库存预警: {} 条", newLowWarnings, newHighWarnings);
        } catch (Exception e) {
            log.error("库存预警检查失败", e);
        }
    }

    /**
     * 每天凌晨3点清空预警记录，重新检查
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void resetWarningCache() {
        lowStockWarnedIds.clear();
        highStockWarnedIds.clear();
        log.info("库存预警缓存已重置");
    }
}
