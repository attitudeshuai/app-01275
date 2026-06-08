package com.wms.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.BusinessException;
import com.wms.dto.LoginDTO;
import com.wms.dto.PasswordDTO;
import com.wms.dto.RegisterDTO;
import com.wms.entity.SysCaptcha;
import com.wms.entity.SysMenu;
import com.wms.entity.SysUser;
import com.wms.entity.SysUserRole;
import com.wms.mapper.SysCaptchaMapper;
import com.wms.mapper.SysMenuMapper;
import com.wms.mapper.SysUserMapper;
import com.wms.mapper.SysUserRoleMapper;
import com.wms.util.JwtUtil;
import com.wms.util.UserContext;
import com.wms.vo.CaptchaVO;
import com.wms.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysCaptchaMapper captchaMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;

    @Value("${security.default-role-id:3}")
    private Long defaultRoleId;

    public CaptchaVO getCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        String uuid = IdUtil.simpleUUID();
        String code = captcha.getCode();
        
        SysCaptcha entity = new SysCaptcha();
        entity.setUuid(uuid);
        entity.setCode(code);
        entity.setExpireTime(LocalDateTime.now().plusMinutes(5));
        captchaMapper.insert(entity);
        
        CaptchaVO vo = new CaptchaVO();
        vo.setUuid(uuid);
        vo.setImage(captcha.getImageBase64Data());
        return vo;
    }

    public String login(LoginDTO dto) {
        SysCaptcha captcha = captchaMapper.selectOne(
            new LambdaQueryWrapper<SysCaptcha>().eq(SysCaptcha::getUuid, dto.getUuid()));
        if (captcha == null) {
            throw new BusinessException("验证码已过期");
        }
        captchaMapper.deleteById(captcha.getId());
        if (!captcha.getCode().equalsIgnoreCase(dto.getCaptcha())) {
            throw new BusinessException("验证码错误");
        }
        if (captcha.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期");
        }

        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        log.info("用户登录成功: {}", user.getUsername());
        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    public UserInfoVO getUserInfo() {
        Long userId = UserContext.getUserId();
        SysUser user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        List<String> perms = menuMapper.selectPermsByUserId(userId);

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setDeptName(user.getDeptName());
        vo.setMenus(buildMenuTree(menus));
        vo.setPermissions(perms);
        return vo;
    }

    public void updatePassword(PasswordDTO dto) {
        Long userId = UserContext.getUserId();
        SysUser user = userMapper.selectById(userId);
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户修改密码: {}", user.getUsername());
    }

    @Transactional
    public void register(RegisterDTO dto) {
        // 验证验证码
        SysCaptcha captcha = captchaMapper.selectOne(
            new LambdaQueryWrapper<SysCaptcha>().eq(SysCaptcha::getUuid, dto.getUuid()));
        if (captcha == null) {
            throw new BusinessException("验证码已过期");
        }
        captchaMapper.deleteById(captcha.getId());
        if (!captcha.getCode().equalsIgnoreCase(dto.getCaptcha())) {
            throw new BusinessException("验证码错误");
        }
        if (captcha.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期");
        }

        // 验证两次密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        SysUser existing = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        // 分配默认角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRoleId);
        userRoleMapper.insert(userRole);

        log.info("新用户注册成功: {}", user.getUsername());
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream().collect(Collectors.toMap(SysMenu::getId, m -> m));
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == 0) {
                tree.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        return tree;
    }
}
