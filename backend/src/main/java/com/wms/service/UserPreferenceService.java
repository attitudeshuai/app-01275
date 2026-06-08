package com.wms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.entity.SysUserPreference;
import com.wms.mapper.SysUserPreferenceMapper;
import com.wms.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final SysUserPreferenceMapper preferenceMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_HISTORY_SIZE = 50;

    /**
     * 获取用户的商品选择历史
     */
    public List<Long> getGoodsHistory(String type) {
        Long userId = UserContext.getUserId();
        String prefKey = type + "_GOODS_HISTORY";
        
        SysUserPreference pref = preferenceMapper.selectByUserIdAndKey(userId, prefKey);
        if (pref == null || pref.getPrefValue() == null) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(pref.getPrefValue(), new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("Failed to parse goods history", e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存用户的商品选择历史
     */
    public void saveGoodsHistory(String type, List<Long> goodsIds) {
        Long userId = UserContext.getUserId();
        String prefKey = type + "_GOODS_HISTORY";
        
        try {
            // 合并新选择的商品ID，保留最近的记录
            List<Long> existingHistory = getGoodsHistory(type);
            Set<Long> mergedSet = new LinkedHashSet<>(goodsIds);
            mergedSet.addAll(existingHistory);
            
            List<Long> newHistory = new ArrayList<>(mergedSet);
            if (newHistory.size() > MAX_HISTORY_SIZE) {
                newHistory = newHistory.subList(0, MAX_HISTORY_SIZE);
            }
            
            String jsonValue = objectMapper.writeValueAsString(newHistory);
            
            SysUserPreference pref = preferenceMapper.selectByUserIdAndKey(userId, prefKey);
            if (pref == null) {
                pref = new SysUserPreference();
                pref.setUserId(userId);
                pref.setPrefKey(prefKey);
                pref.setPrefValue(jsonValue);
                pref.setCreateTime(LocalDateTime.now());
                pref.setUpdateTime(LocalDateTime.now());
                preferenceMapper.insert(pref);
            } else {
                pref.setPrefValue(jsonValue);
                pref.setUpdateTime(LocalDateTime.now());
                preferenceMapper.updateById(pref);
            }
            
            log.debug("Saved goods history for user {}: {} items", userId, newHistory.size());
        } catch (Exception e) {
            log.error("Failed to save goods history", e);
        }
    }

    /**
     * 清除用户的商品选择历史
     */
    public void clearGoodsHistory(String type) {
        Long userId = UserContext.getUserId();
        String prefKey = type + "_GOODS_HISTORY";
        
        SysUserPreference pref = preferenceMapper.selectByUserIdAndKey(userId, prefKey);
        if (pref != null) {
            preferenceMapper.deleteById(pref.getId());
        }
    }
}
