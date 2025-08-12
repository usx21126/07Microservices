package com.hmall.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

//@Component
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory<PrintAnyGatewayFilterFactory.Config> {
    @Override
    public GatewayFilter apply(Config config) {
        /*return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 获取请求信息
                ServerHttpRequest request = exchange.getRequest();
                // 处理过滤业务逻辑
                System.out.println("PrintAnyGatewayFilterFactory 执行了");
                // 放行
                return chain.filter(exchange);
            }
        };*/
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 获取配置的属性值
                String a = config.getA();
                String b = config.getB();
                String c = config.getC();
                System.out.println(" a = " + a);
                System.out.println(" b = " + b);
                System.out.println(" c = " + c);

                return chain.filter(exchange);
            }
        }, 100);
    }

    //定义内部配置类；里面包含过滤器自定义的配置属性
    @Data
    public static class Config {
        private String a;
        private String b;
        private String c;
    }
    //将变量名依次按顺序返回；取对应参数时也需要按照顺序取

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("a", "b", "c");
    }

    // 将Config字节码传递给父类，父类负责帮我们读取yaml配置
    public PrintAnyGatewayFilterFactory(){
        super(Config.class);
    }

}
