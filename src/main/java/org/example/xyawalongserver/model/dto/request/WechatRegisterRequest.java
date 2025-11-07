package org.example.xyawalongserver.model.dto.request;

import lombok.Data;

@Data
public class WechatRegisterRequest {
    private String openid;
    private String nickname;
    private String avatarUrl;
    // getter/setter
}