package org.example.xyawalongserver.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.xyawalongserver.util.JwtTokenUtil;
import org.example.xyawalongserver.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        System.out.println(token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtTokenUtil.validateToken(token)) {
                System.out.println("✅ JWT验证通过");
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                System.out.println("🔍 从Token中获取的userId: " + userId);

                request.setAttribute("userId", userId);
                UserContext.setCurrentUserId(userId); // 设置到 ThreadLocal
                return true;
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String errorResponse = "{\"code\": 401, \"message\": \"Token验证失败\", \"data\": null}";
        response.getWriter().write(errorResponse);
        response.getWriter().flush();

        return false; // 中断请求，不再继续执行
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理 ThreadLocal，避免内存泄漏
        UserContext.clear();
    }
}