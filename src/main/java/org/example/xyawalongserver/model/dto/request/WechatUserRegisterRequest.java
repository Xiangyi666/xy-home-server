package org.example.xyawalongserver.model.dto.request;

public class WechatUserRegisterRequest {
    private String wechatOpenid;     // 微信openid
    private String wechatUnionid;    // 微信unionid（可选）
    private String nickname;         // 微信昵称
    private String avatarUrl;        // 微信头像
    private String phoneNumber;      // 手机号（可选）

    // Getter和Setter
    public String getWechatOpenid() { return wechatOpenid; }
    public void setWechatOpenid(String wechatOpenid) { this.wechatOpenid = wechatOpenid; }

    public String getWechatUnionid() { return wechatUnionid; }
    public void setWechatUnionid(String wechatUnionid) { this.wechatUnionid = wechatUnionid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}