package com.hmall.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、获取请求信息
        ServerHttpRequest request = exchange.getRequest();
        //2、处理请求信息
        System.out.println("MyGlobalFilter pre阶段 执行了。 请求路径：" + request.getPath());
        //3、放行
        return chain.filter(exchange).then(Mono.fromRunnable(()->{
            System.out.println("MyGlobalFilter post阶段 执行了。");
        }));
    }

    @Override
    public int getOrder() {
        // 返回值越小，越先执行
        return 0;
    }
}
