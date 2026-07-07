package com.sjqcf.interceptor;

import com.sjqcf.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    // 在业务处理器处理请求之前被调用
    // 如果返回false，则中断后续操作, 返回true，表示继续执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");
        //获取请求路径
        String path = request.getRequestURI();

        // 浏览器语音 WebSocket 升级请求：token 在握手查询参数中校验，此处放行
        if (path.contains("/ws/voice")) {
            return true;
        }

        // 判断请求路径是否包含login
        if (path.contains("login")) {
            log.info("用户登录, 无需拦截");
            return true;
        }

        // 获取token
        String token = request.getHeader("token");
        if (token == null) {
            log.info("token为空");
            return false;
        }

        // 验证token
        try {
            // 验证token
            JwtUtils.parseJwt(token);
        } catch (Exception e) {
            log.info("token验证失败,响应401");
            response.setStatus(401);
            return false;
        }

        // 验证成功
        log.info("token验证成功");
        return true;
    }

    // 在业务处理器处理请求执行完成后，生成视图之前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
}
