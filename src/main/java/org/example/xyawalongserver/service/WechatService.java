package org.example.xyawalongserver.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.example.xyawalongserver.model.dto.response.WechatAccessTokenResponse;
import org.example.xyawalongserver.model.dto.response.WechatAuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WechatService {

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    private static final String WECHAT_AUTH_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String WECHAT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    /**
     * 用 code 换取 openid
     */
    public String getOpenidByCode(String code) {
        try {
            // 构建请求URL
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    WECHAT_AUTH_URL, appId, appSecret, code);

            // 发送HTTP请求
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<WechatAuthResponse> response = restTemplate.getForEntity(url, WechatAuthResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WechatAuthResponse authResponse = response.getBody();

                if (authResponse.getErrcode() != null && authResponse.getErrcode() != 0) {
                    throw new RuntimeException("微信认证失败: " + authResponse.getErrmsg());
                }

                return authResponse.getOpenid();
            } else {
                throw new RuntimeException("微信认证服务异常");
            }

        } catch (Exception e) {
            throw new RuntimeException("获取openid失败: " + e.getMessage());
        }
    }

    /**
     * 用 code 换取 openid 和 session_key
     */
    public WechatAuthResponse getAuthInfoByCode(String code) {
        try {
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    WECHAT_AUTH_URL, appId, appSecret, code);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<WechatAuthResponse> response = restTemplate.getForEntity(url, WechatAuthResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WechatAuthResponse authResponse = response.getBody();

                if (authResponse.getErrcode() != null && authResponse.getErrcode() != 0) {
                    throw new RuntimeException("微信认证失败: " + authResponse.getErrmsg());
                }

                return authResponse;
            } else {
                throw new RuntimeException("微信认证服务异常");
            }

        } catch (Exception e) {
            throw new RuntimeException("获取微信认证信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取微信 access_token（用于其他微信API调用）
     */
    public String getAccessToken() {
        try {
            String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                    WECHAT_ACCESS_TOKEN_URL, appId, appSecret);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<WechatAccessTokenResponse> response = restTemplate.getForEntity(url, WechatAccessTokenResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WechatAccessTokenResponse tokenResponse = response.getBody();

                if (tokenResponse.getErrcode() != null && tokenResponse.getErrcode() != 0) {
                    throw new RuntimeException("获取access_token失败: " + tokenResponse.getErrmsg());
                }

                return tokenResponse.getAccessToken();
            } else {
                throw new RuntimeException("获取access_token服务异常");
            }

        } catch (Exception e) {
            throw new RuntimeException("获取access_token失败: " + e.getMessage());
        }
    }

    /**
     * 验证微信用户数据（可选，用于解密加密数据）
     */
    public boolean verifyUserInfo(String rawData, String signature, String sessionKey) {
        try {
            // 验证签名：sha1( rawData + sessionKey )
            String computedSignature = DigestUtils.sha1Hex(rawData + sessionKey);
            return computedSignature.equals(signature);
        } catch (Exception e) {
            throw new RuntimeException("验证用户数据失败: " + e.getMessage());
        }
    }
}