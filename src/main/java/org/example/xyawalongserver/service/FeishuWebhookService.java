package org.example.xyawalongserver.service;

import org.example.xyawalongserver.model.entity.FeishuWebhookRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class FeishuWebhookService {

    private final WebClient webClient;

    // 从配置文件中读取飞书Webhook URL
    @Value("${webhook.feishu.url:}")
    private String feishuWebhookUrl;

    public FeishuWebhookService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 发送文本消息到飞书机器人
     * @param message 文本内容
     * @return 飞书API的响应
     */
    public String sendTextMessage(String message) {
        FeishuWebhookRequest request = new FeishuWebhookRequest(message);
        return sendWebhookRequest(request);
    }

    /**
     * 发送富文本消息到飞书机器人
     * @param title 标题
     * @param content 内容（支持简单的HTML标签）
     * @return 飞书API的响应
     */
    public String sendRichTextMessage(String title, String content) {
        // 构建富文本内容结构
        Map<String, Object> postContent = new HashMap<>();

        Map<String, Object> postBody = new HashMap<>();
        postBody.put("title", title);

        // 内容是一个二维数组，每个元素是一个内容块
        Object[][] contentArray = new Object[1][1];
        Map<String, Object> tagMap = new HashMap<>();
        tagMap.put("tag", "text");
        tagMap.put("text", content);
        contentArray[0][0] = tagMap;

        postBody.put("content", contentArray);
        postContent.put("post", postBody);

        FeishuWebhookRequest request = new FeishuWebhookRequest(postContent);
        return sendWebhookRequest(request);
    }

    /**
     * 发送通用Webhook请求
     * @param request 飞书消息请求体
     * @return 响应结果
     */
    private String sendWebhookRequest(FeishuWebhookRequest request) {
        if (feishuWebhookUrl == null || feishuWebhookUrl.trim().isEmpty()) {
            throw new IllegalStateException("飞书Webhook URL未配置，请在application.properties中设置webhook.feishu.url");
        }

        return webClient.post()
                .uri(feishuWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 同步等待结果
    }

    /**
     * 异步发送消息
     * @param message 文本消息
     * @return Mono对象，包含异步响应
     */
    public Mono<String> sendTextMessageAsync(String message) {
        FeishuWebhookRequest request = new FeishuWebhookRequest(message);

        return webClient.post()
                .uri(feishuWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }
}