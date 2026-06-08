package com.wms.vo;

import com.wms.entity.SysMenu;
import lombok.Data;
import java.util.List;

@Data
public class UserInfoVO {
    private Long userId;
    private String username;
    private String realName;
    private String deptName;
    private List<SysMenu> menus;
    private List<String> permissions;
}
