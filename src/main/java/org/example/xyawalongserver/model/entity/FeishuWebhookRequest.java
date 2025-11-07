package org.example.xyawalongserver.model.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * 飞书机器人Webhook消息请求体
 * 支持文本、富文本（post）、卡片等多种格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeishuWebhookRequest {

    /**
     * 消息类型：text, post, interactive（卡片）等
     */
    private String msg_type;

    /**
     * 文本内容（当 msg_type = "text" 时使用）
     */
    private Content content;

    /**
     * 富文本内容（当 msg_type = "post" 时使用）
     */
    private RichContent post;

    // 静态内部类：文本消息内容
    public static class Content {
        private String text;

        public Content(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    // 静态内部类：富文本消息内容
    public static class RichContent {
        private Map<String, Object> zh_cn;

        public RichContent(Map<String, Object> zh_cn) {
            this.zh_cn = zh_cn;
        }

        public Map<String, Object> getZh_cn() {
            return zh_cn;
        }

        public void setZh_cn(Map<String, Object> zh_cn) {
            this.zh_cn = zh_cn;
        }
    }

    // 构造函数 - 文本消息
    public FeishuWebhookRequest(String textContent) {
        this.msg_type = "text";
        this.content = new Content(textContent);
    }

    // 构造函数 - 富文本消息
    public FeishuWebhookRequest(Map<String, Object> postContent) {
        this.msg_type = "post";
        this.post = new RichContent(postContent);
    }

    // Getter 和 Setter
    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public RichContent getPost() {
        return post;
    }

    public void setPost(RichContent post) {
        this.post = post;
    }
}