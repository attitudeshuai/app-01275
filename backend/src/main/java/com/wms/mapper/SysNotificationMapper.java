package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysNotificationMapper extends BaseMapper<SysNotification> {
    
    @Select("SELECT * FROM sys_notification WHERE (user_id = #{userId} OR user_id IS NULL) AND status = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<SysNotification> selectUnreadByUserId(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Select("SELECT COUNT(*) FROM sys_notification WHERE (user_id = #{userId} OR user_id IS NULL) AND status = 0")
    int countUnreadByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE sys_notification SET status = 1, read_time = NOW() WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);
    
    @Update("UPDATE sys_notification SET status = 1, read_time = NOW() WHERE (user_id = #{userId} OR user_id IS NULL) AND status = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
