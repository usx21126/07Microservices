package com.hmall.gateway.filter;

import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.util.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component
@EnableConfigurationProperties(AuthProperties.class)
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final JwtTool jwtTool;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、判断是否要拦截
        ServerHttpRequest request = exchange.getRequest();
        if (isExclude(request.getPath().toString())) {
            //无需拦截；直接放行
            return chain.filter(exchange);
        }
        //2、获取token
        String token = request.getHeaders().getFirst("authorization");
        //3、校验token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
            //4、todo 传递用户信息
            System.out.println(" userId = " + userId);
        } catch (Exception e) {
            //token校验失败
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //4. 传递用户信息到下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("user-info", userId.toString())
                .build();

        //5、放行修改后的请求
        System.out.println("AuthGlobalFilter执行了 userId" + userId);
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
