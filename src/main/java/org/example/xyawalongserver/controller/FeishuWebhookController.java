package org.example.xyawalongserver.controller;


import  org.example.xyawalongserver.service.FeishuWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feishu")
public class FeishuWebhookController {

    @Autowired
    private FeishuWebhookService feishuWebhookService;

    /**
     * 发送文本消息 - GET方式（方便测试）
     */
    @GetMapping("/send-text")
    public ResponseEntity<Map<String, Object>> sendTextMessageGet(@RequestParam String message) {
        try {
            String result = feishuWebhookService.sendTextMessage(message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发送文本消息 - POST方式（推荐用于生产）
     */
    @PostMapping("/send-text")
    public ResponseEntity<Map<String, Object>> sendTextMessagePost(@RequestBody Map<String, String> requestBody) {
        try {
            String message = requestBody.get("message");
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("消息内容不能为空");
            }

            String result = feishuWebhookService.sendTextMessage(message);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发送富文本消息
     */
    @PostMapping("/send-rich-text")
    public ResponseEntity<Map<String, Object>> sendRichTextMessage(@RequestBody Map<String, String> requestBody) {
        try {
            String title = requestBody.get("title");
            String content = requestBody.get("content");

            if (title == null || content == null) {
                throw new IllegalArgumentException("标题和内容都不能为空");
            }

            String result = feishuWebhookService.sendRichTextMessage(title, content);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}