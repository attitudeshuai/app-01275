package com.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.dto.LoginDTO;
import com.wms.entity.SysCaptcha;
import com.wms.mapper.SysCaptchaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysCaptchaMapper captchaMapper;

    private String testUuid;
    private String testCode;

    @BeforeEach
    void setUp() {
        testUuid = "test-uuid-" + System.currentTimeMillis();
        testCode = "TEST";
        SysCaptcha captcha = new SysCaptcha();
        captcha.setUuid(testUuid);
        captcha.setCode(testCode);
        captcha.setExpireTime(LocalDateTime.now().plusMinutes(5));
        captchaMapper.insert(captcha);
    }

    @Test
    @DisplayName("获取验证码")
    void testGetCaptcha() throws Exception {
        mockMvc.perform(get("/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uuid").exists())
                .andExpect(jsonPath("$.data.image").exists());
    }

    @Test
    @DisplayName("登录成功")
    void testLoginSuccess() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("admin123");
        loginDTO.setUuid(testUuid);
        loginDTO.setCaptcha(testCode);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("登录失败-验证码错误")
    void testLoginFailWithWrongCaptcha() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("admin123");
        loginDTO.setUuid(testUuid);
        loginDTO.setCaptcha("WRONG");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("验证码错误"));
    }

    @Test
    @DisplayName("登录失败-用户名或密码错误")
    void testLoginFailWithWrongCredentials() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrongpassword");
        loginDTO.setUuid(testUuid);
        loginDTO.setCaptcha(testCode);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录失败-参数校验")
    void testLoginFailWithValidation() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(""); // 空用户名
        loginDTO.setPassword("admin123");
        loginDTO.setUuid(testUuid);
        loginDTO.setCaptcha(testCode);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
