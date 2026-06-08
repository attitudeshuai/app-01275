package com.wms.service;

import com.wms.entity.SysNotification;
import com.wms.mapper.SysNotificationMapper;
import com.wms.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SysNotificationMapper notificationMapper;

    /**
     * 创建通知
     */
    public void createNotification(String title, String content, String type, Long userId, Long refId, String refType) {
        SysNotification notification = new SysNotification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setUserId(userId);
        notification.setRefId(refId);
        notification.setRefType(refType);
        notification.setStatus(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        log.info("创建通知: {} - {}", title, type);
    }

    /**
     * 创建低库存预警通知（发送给所有用户）
     */
    public void createLowStockWarningNotification(String goodsName, int currentQty, int safetyStock, Long goodsId) {
        String title = "库存不足预警";
        String content = String.format("商品【%s】库存不足，当前库存：%d，安全库存：%d，请及时补货！", 
                                       goodsName, currentQty, safetyStock);
        createNotification(title, content, "STOCK_WARNING", null, goodsId, "GOODS");
    }

    /**
     * 创建高库存预警通知（发送给所有用户）
     */
    public void createHighStockWarningNotification(String goodsName, int currentQty, int maxStock, Long goodsId) {
        String title = "库存超量预警";
        String content = String.format("商品【%s】库存超量，当前库存：%d，最大库存：%d，请注意库存积压风险！", 
                                       goodsName, currentQty, maxStock);
        createNotification(title, content, "STOCK_OVERFLOW", null, goodsId, "GOODS");
    }

    /**
     * 创建库存预警通知（发送给所有用户）- 兼容旧方法
     */
    public void createStockWarningNotification(String goodsName, int currentQty, int safetyStock, Long goodsId) {
        createLowStockWarningNotification(goodsName, currentQty, safetyStock, goodsId);
    }

    /**
     * 获取当前用户未读通知
     */
    public List<SysNotification> getUnreadNotifications(int limit) {
        Long userId = UserContext.getUserId();
        return notificationMapper.selectUnreadByUserId(userId, limit);
    }

    /**
     * 获取当前用户未读通知数量
     */
    public int getUnreadCount() {
        Long userId = UserContext.getUserId();
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 标记通知为已读
     */
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }

    /**
     * 标记所有通知为已读
     */
    public void markAllAsRead() {
        Long userId = UserContext.getUserId();
        notificationMapper.markAllAsRead(userId);
    }
}
