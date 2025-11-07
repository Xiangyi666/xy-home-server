package org.example.xyawalongserver.controller;

import org.example.xyawalongserver.model.dto.request.WechatCodeRequest;
import org.example.xyawalongserver.model.dto.request.WechatOpenidRequest;
import org.example.xyawalongserver.model.dto.request.WechatRegisterRequest;
import org.example.xyawalongserver.model.dto.request.WechatUserRegisterRequest;
import org.example.xyawalongserver.model.entity.User;
import org.example.xyawalongserver.service.UserService;
import org.example.xyawalongserver.service.WechatService;
import org.example.xyawalongserver.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> checkWechatUser(@RequestParam String openid) {
        try {
            boolean exists = userService.findByWechatOpenid(openid).isPresent();

            Map<String, Object> response = new HashMap<>();
            response.put("registered", exists);
            response.put("openid", openid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "检查用户失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 用 openid 登录
     */
    @PostMapping("/wechat/login-by-openid")
    public ResponseEntity<?> loginByOpenid(@RequestBody WechatOpenidRequest request) {
        try {
            User user = userService.findByWechatOpenid(request.getOpenid())
                    .orElseThrow(() -> new RuntimeException("用户未注册"));

            String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            response.put("message", "登录成功");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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
