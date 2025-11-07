package org.example.xyawalongserver.controller;

import org.example.xyawalongserver.model.entity.WechatWorkMessage;
import org.example.xyawalongserver.model.entity.WechatWorkResponse;
import org.example.xyawalongserver.service.WechatWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/api/wechat")
public class WechatWorkController {

    @Autowired
    private WechatWorkService wechatWorkService;

    /**
     * 发送文本消息到默认Webhook
     */
    @PostMapping("/send-text")
    public Mono<ResponseEntity<String>> sendTextMessage(@RequestParam String message) {
        return wechatWorkService.sendTextMessage(message)
                .map(response -> ResponseEntity.ok("消息发送成功"))
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.badRequest().body("发送失败: " + error.getMessage())));
    }

    /**
     * 发送文本消息到指定Webhook
     */
    @PostMapping("/send-text-to")
    public Mono<ResponseEntity<String>> sendTextMessageTo(
            @RequestParam String webhookUrl,
            @RequestParam String message) {
        return wechatWorkService.sendTextMessage(webhookUrl, message)
                .map(response -> ResponseEntity.ok("消息发送成功"))
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.badRequest().body("发送失败: " + error.getMessage())));
    }

    /**
     * 发送Markdown消息
     */
    @PostMapping("/send-markdown")
    public Mono<ResponseEntity<String>> sendMarkdownMessage(
            @RequestParam(required = false) String webhookUrl,
            @RequestParam String message) {

        Mono<WechatWorkResponse> sendMono;
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            sendMono = wechatWorkService.sendMarkdownMessage(webhookUrl, message);
        } else {
            sendMono = wechatWorkService.sendMarkdownMessage(message);
        }

        return sendMono
                .map(response -> ResponseEntity.ok("Markdown消息发送成功"))
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.badRequest().body("发送失败: " + error.getMessage())));
    }

    /**
     * 通过JSON发送自定义消息
     */
    @PostMapping("/send-custom")
    public Mono<ResponseEntity<String>> sendCustomMessage(@RequestBody CustomMessageRequest request) {
        return wechatWorkService.sendMessage(request.getWebhookUrl(), request.getMessage())
                .map(response -> ResponseEntity.ok("消息发送成功"))
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.badRequest().body("发送失败: " + error.getMessage())));
    }

    // 请求体类
    public static class CustomMessageRequest {
        private String webhookUrl;
        private WechatWorkMessage message;

        // getter 和 setter
        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public WechatWorkMessage getMessage() {
            return message;
        }

        public void setMessage(WechatWorkMessage message) {
            this.message = message;
        }
    }
}