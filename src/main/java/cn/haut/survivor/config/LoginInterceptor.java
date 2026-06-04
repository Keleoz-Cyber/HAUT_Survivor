package cn.haut.survivor.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
    public static final String LOGIN_USER_ROLE = "LOGIN_USER_ROLE";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getSession().getAttribute(LOGIN_USER_ID) != null) {
            return true;
        }
        response.sendRedirect("/login");
        return false;
    }
}
