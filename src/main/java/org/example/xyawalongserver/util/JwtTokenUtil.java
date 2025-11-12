package org.example.xyawalongserver.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {

    // 使用安全的密钥生成
    private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private static long expiration = 86400000; // 24小时
    private static long REFRESH_TOKEN_EXPIRATION = 86400000 * 7; // 24小时
    /**
     * 获取Access Token的过期时间（毫秒）
     */
    public static long getAccessTokenExpirationTime() {
        return 86400000;
    }
    public static String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS512) // 明确指定算法
                .compact();
    }
    /**
     * 生成Refresh Token（长期）
     */
    public static String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    // 获取过期时间
    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }
    public static Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }

/**
 * 从已过期的JWT令牌中提取用户名
 * @param token 已过期的JWT令牌字符串
 * @return 用户名，即使令牌已过期
 * @throws RuntimeException 当令牌无法解析时抛出运行时异常
 */
    public static String getUsernameFromExpiredToken(String token) {
        System.out.println(token);
        try {
        // 尝试从正常令牌中获取用户名
            return getUsernameFromToken(token);
        } catch (ExpiredJwtException e) {
            // 如果令牌已过期，捕获异常并从过期令牌的声明中获取用户名
            System.out.println("ExpiredJwtException");
            System.out.println(e.getMessage());

            // 即使过期也返回用户名
            return e.getClaims().getSubject();
        } catch (Exception e) {
        // 处理其他类型的异常，抛出运行时异常提示令牌解析失败
            throw new RuntimeException("无法解析token");
        }
    }
    // 或者更简化的版本
    @Data
    @AllArgsConstructor
    public static class TokenUserInfo {
        private String username;
        private Long userId;
    }
    // 获取简化的用户信息
    public static TokenUserInfo getUserInfoFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String username = claims.getSubject();
        Long userId = getUserIdFromToken(token);
        return new TokenUserInfo(username, userId);
    }

    // 安全的token刷新方法
    public static String refreshAccessToken(String oldAccessToken, String refreshToken) {
        try {
            // 验证refresh token是否有效
            if (isTokenExpired(refreshToken)) {
                throw new RuntimeException("Refresh token已过期");
            }

            // 从过期的access token中获取用户名
            String username = getUsernameFromExpiredToken(oldAccessToken);

            // 验证refresh token中的用户名是否匹配
            String refreshTokenUsername = getUsernameFromToken(refreshToken);
            if (!username.equals(refreshTokenUsername)) {
                throw new RuntimeException("Token不匹配");
            }

            TokenUserInfo userInfo = getUserInfoFromToken(refreshToken);
            return generateToken(userInfo.getUserId(),userInfo.getUsername());

        } catch (Exception e) {
            throw new RuntimeException("Token刷新失败: " + e.getMessage());
        }
    }
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        System.out.println(claims);
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("JWT 验证失败: " + e.getMessage());
            return false;
        }
    }

    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }


    private static Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    private Date calculateExpirationDate(Date createdDate) {
        return new Date(createdDate.getTime() + expiration);
    }
}