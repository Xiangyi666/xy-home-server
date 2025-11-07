package org.example.xyawalongserver.service;

import org.example.xyawalongserver.model.entity.WechatWorkMessage;
import org.example.xyawalongserver.model.entity.WechatWorkResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WechatWorkService {

    private final WebClient webClient;

    @Value("${wechat.webhook.url:}")
    private String defaultWebhookUrl;

    public WechatWorkService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 发送文本消息到默认Webhook
     */
    public Mono<WechatWorkResponse> sendTextMessage(String content) {
        if (defaultWebhookUrl.isEmpty()) {
            return Mono.error(new IllegalArgumentException("默认Webhook URL未配置"));
        }
        return sendTextMessage(defaultWebhookUrl, content);
    }

    /**
     * 发送文本消息到指定Webhook
     */
    public Mono<WechatWorkResponse> sendTextMessage(String webhookUrl, String content) {
        WechatWorkMessage message = WechatWorkMessage.textMessage(content);
        return sendMessage(webhookUrl, message);
    }

    /**
     * 发送Markdown消息到默认Webhook
     */
    public Mono<WechatWorkResponse> sendMarkdownMessage(String content) {
        if (defaultWebhookUrl.isEmpty()) {
            return Mono.error(new IllegalArgumentException("默认Webhook URL未配置"));
        }
        return sendMarkdownMessage(defaultWebhookUrl, content);
    }

    /**
     * 发送Markdown消息到指定Webhook
     */
    public Mono<WechatWorkResponse> sendMarkdownMessage(String webhookUrl, String content) {
        WechatWorkMessage message = WechatWorkMessage.markdownMessage(content);
        return sendMessage(webhookUrl, message);
    }

    /**
     * 发送消息到企业微信Webhook
     */
    public Mono<WechatWorkResponse> sendMessage(String webhookUrl, WechatWorkMessage message) {
        return webClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .bodyToMono(WechatWorkResponse.class)
                .doOnSuccess(response -> {
                    if (!response.isSuccess()) {
                        throw new RuntimeException(
                                String.format("企业微信消息发送失败: [%d] %s",
                                        response.getErrCode(), response.getErrMsg()));
                    }
                })
                .doOnError(error -> {
                    // 记录错误日志
                    System.err.println("发送企业微信消息时发生错误: " + error.getMessage());
                });
    }

    /**
     * 异步发送消息（不等待响应）
     */
    public void sendMessageAsync(String webhookUrl, WechatWorkMessage message) {
        sendMessage(webhookUrl, message)
                .subscribe(
                        response -> System.out.println("消息发送成功"),
                        error -> System.err.println("消息发送失败: " + error.getMessage())
                );
    }
}