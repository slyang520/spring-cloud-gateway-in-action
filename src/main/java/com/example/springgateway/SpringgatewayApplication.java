package com.example.springgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class SpringgatewayApplication {

//
//    Route（路由）：这是网关的基本构建块。它由一个 ID，一个目标 URI，一组断言和一组过滤器定义。如果断言为真，则路由匹配。
//
//    Predicate（断言）：这是一个 Java 8 的 Predicate。输入类型是一个 ServerWebExchange。我们可以使用它来匹配来自 HTTP 请求的任何内容，例如 headers 或参数。
//
//    Filter（过滤器）：这是org.springframework.cloud.gateway.filter.GatewayFilter的实例，我们可以使用它修改请求和响应。
//


    public static void main(String[] args) {
        SpringApplication.run(SpringgatewayApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                //   添加请求头
                //   >>> http://localhost:8080/get
                //   <<< http://httpbin.org:80/get
                .route(p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri("http://httpbin.org:80"))


                //   路径重写
                //   >>> http://localhost:8080/xxapi/get
                //   <<< http://httpbin.org:80/get
                .route(p -> p
                        .path("/xxapi/**")
                        .filters(f -> f.rewritePath("/xxapi/(?<segment>.*)","/${segment}"))
                        .uri("http://httpbin.org"))

                
                // 配合 HystrixCommand   hystrix 演示网关超时
                // curl --dump-header - --header 'Host: www.hystrix.com' http://localhost:8080/delay/3
                .route(p -> p
                        .host("*.hystrix.com")
                        .filters(f ->
                                        f.hystrix(config -> config
                                        .setName("mycmd")
                                        .setFallbackUri("forward:/fallback"))
                                )
                        .uri("http://httpbin.org:80"))

                .build();
    }

    @GetMapping("/fallback")
    public Mono<String> fallback() {

        System.out.println("fdfa");
        
        return Mono.just("\nI AM  fallback\n");
    }


}
