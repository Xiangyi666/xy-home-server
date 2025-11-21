package org.example.xyawalongserver.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.example.xyawalongserver.model.dto.request.WechatCodeRequest;
import org.example.xyawalongserver.model.dto.request.WechatOpenidRequest;
import org.example.xyawalongserver.model.dto.request.WechatRegisterRequest;
import org.example.xyawalongserver.model.dto.request.WechatUserRegisterRequest;
import org.example.xyawalongserver.model.entity.User;
import org.example.xyawalongserver.service.UserService;
import org.example.xyawalongserver.service.WechatService;
import org.example.xyawalongserver.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private WechatService wechatService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    /**
     * 用 code 换取 openid
     */
    @PostMapping("/wechat/get-openid")
    public ResponseEntity<?> getOpenidByCode(@RequestBody WechatCodeRequest request) {
        try {
            String openid = wechatService.getOpenidByCode(request.getCode());

            Map<String, Object> response = new HashMap<>();
            response.put("openid", openid);
            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取openid失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 检查用户是否已注册
     */
    @GetMapping("/wechat/check")
    public ApiResponse<?> checkWechatUser(@RequestParam String openid) {
        try {
            boolean exists = userService.findByWechatOpenid(openid).isPresent();

            Map<String, Object> response = new HashMap<>();
            response.put("registered", exists);
            response.put("openid", openid);

            return ApiResponse.success(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "检查用户失败: " + e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
        private String accessToken;
    }
    @Data
    @AllArgsConstructor
    @Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
    }
    // 允许过期的token也能解析（用于刷新场景）

    // 刷新token接口
    @PostMapping("/wechat/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String newAccessToken = JwtTokenUtil.refreshAccessToken(refreshTokenRequest.getAccessToken(),refreshTokenRequest.getRefreshToken());

            // 使用JWTUtil刷新token
            // 返回新的token

            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshTokenRequest.getRefreshToken()) // 或者生成新的refresh token
                    .tokenType("Bearer")
                    .expiresIn(JwtTokenUtil.getAccessTokenExpirationTime())
                    .build();
            return ResponseEntity.ok(tokenResponse);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    // 请求体类
    public static class UpdateUsernameRequest {
        private String newUsername;

        // getter 和 setter
        public String getNewUsername() {
            return newUsername;
        }

        public void setNewUsername(String newUsername) {
            this.newUsername = newUsername;
        }
    }
    @PostMapping("/updateUserName")
    public ApiResponse<?> updateUsername(@RequestBody UpdateUsernameRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            boolean success = userService.updateUsername(userId, request.getNewUsername());
            if (success) {
                return ApiResponse.success("用户名修改成功");
            } else {
                return ApiResponse.error("用户名修改失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    /**
     * 用 openid 登录
     */
    @PostMapping("/wechat/login-by-openid")
    public ApiResponse<?> loginByOpenid(@RequestBody WechatOpenidRequest request) {
        try {
            User user = userService.findByWechatOpenid(request.getOpenid())
                    .orElseThrow(() -> new RuntimeException("用户未注册"));

            String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("message", "登录成功");
            response.put("refreshToken", refreshToken);

            return ApiResponse.success(response);

        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 注册微信用户
     */
    @PostMapping("/wechat/register")
    public ResponseEntity<?> registerWechatUser(@RequestBody WechatRegisterRequest request) {
        try {
            User user = userService.registerWechatUser(
                    request.getOpenid(),
                    request.getNickname(),
                    request.getAvatarUrl()
            );

            String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("message", "注册成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
