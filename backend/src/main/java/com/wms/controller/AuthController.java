package com.wms.controller;

import com.wms.annotation.Log;
import com.wms.common.Result;
import com.wms.dto.LoginDTO;
import com.wms.dto.PasswordDTO;
import com.wms.dto.RegisterDTO;
import com.wms.service.AuthService;
import com.wms.vo.CaptchaVO;
import com.wms.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        return Result.success(authService.getCaptcha());
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginDTO dto) {
        String token = authService.login(dto);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(authService.getUserInfo());
    }

    @Log(module = "个人中心", operation = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO dto) {
        authService.updatePassword(dto);
        return Result.success();
    }
}
