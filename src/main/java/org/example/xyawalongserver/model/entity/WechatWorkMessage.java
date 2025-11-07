package org.example.xyawalongserver.model.entity;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WechatWorkMessage {

    private String msgtype;
    private Text text;
    private Markdown markdown;

    // 文本消息构造函数
    public static WechatWorkMessage textMessage(String content) {
        WechatWorkMessage message = new WechatWorkMessage();
        message.msgtype = "text";
        message.text = new Text(content);
        return message;
    }

    // Markdown消息构造函数
    public static WechatWorkMessage markdownMessage(String content) {
        WechatWorkMessage message = new WechatWorkMessage();
        message.msgtype = "markdown";
        message.markdown = new Markdown(content);
        return message;
    }

    // getter 和 setter
    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public Markdown getMarkdown() {
        return markdown;
    }

    public void setMarkdown(Markdown markdown) {
        this.markdown = markdown;
    }

    // 内部Text类
    public static class Text {
        private String content;
        private List<String> mentionedList;
        private List<String> mentionedMobileList;

        public Text(String content) {
            this.content = content;
        }

        // getter 和 setter
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getMentionedList() {
            return mentionedList;
        }

        public void setMentionedList(List<String> mentionedList) {
            this.mentionedList = mentionedList;
        }

        public List<String> getMentionedMobileList() {
            return mentionedMobileList;
        }

        public void setMentionedMobileList(List<String> mentionedMobileList) {
            this.mentionedMobileList = mentionedMobileList;
        }
    }

    // 内部Markdown类
    public static class Markdown {
        private String content;

        public Markdown(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}