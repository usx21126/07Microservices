package com.hmall.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1、获取请求头中的用户信息
        String userId = request.getHeader("user-info");
        System.out.println("UserInfoInterceptor执行了 userId"+ userId );
        // 2、将用户信息放入ThreadLocal中
        if (StrUtil.isNotBlank(userId)) {
            // 1、将用户信息放入ThreadLocal中
            UserContext.setUser(Long.parseLong(userId));
        }
        // 3、放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户信息
        UserContext.removeUser();
    }
}
