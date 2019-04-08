package com.example.springgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SpringBootApplication
@RestController
public class SpringgatewayApplication {

//
//    Route（路由）：这是网关的基本构建块。
//                  它由一个 ID，一个目标 URI，一组断言和一组过滤器定义。如果断言为真，则路由匹配。
//
//    Predicate（断言）：这是一个 Java 8 的 Predicate。
//                          输入类型是一个 ServerWebExchange。我们可以使用它来匹配来自 HTTP 请求的任何内容，例如 headers 或参数。
//
//    Filter（过滤器）：这是org.springframework.cloud.gateway.filter.GatewayFilter的实例，
//                      我们可以使用它修改请求和响应。
//
//
//
//
//    id：固定，不同 id 对应不同的功能，可参考 官方文档
//    uri：目标服务地址
//    predicates：路由条件
//    filters：过滤规则
//    https://windmt.com/2019/01/20/spring-cloud-20-gateway-dynamic-routing/

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
                //   等价于 f.stripPrefix(1) //忽略第一段 /xxapi/get  /get
                .route(p -> p
                        .path("/xxapi/**")
                        .filters(f -> f.rewritePath("/xxapi/(?<segment>.*)","/${segment}"))
                        .uri("http://httpbin.org"))


                // curl 127.0.0.1:8080/weighttest/1
                // 权重测试
                .route("id_360",p -> p
                        .path("/weighttest/**")
                        .and()
                        .weight("service1",1)
                        .uri("http://360.com"))
                .route("id_baidu",p -> p
                        .path("/weighttest/**")
                        .and()
                        .weight("service1",1)
                        .uri("http://baidu.com"))


                // 限流
                // curl http://localhost:8080/request/rateLimit
                .route(p -> p
                        .path("/request/**")
                        .filters(f ->
                                        f.stripPrefix(1).filter(new RateLimitByIpGatewayFilter(10,1,Duration.ofSeconds(1)))
                                )
                        .uri("http://localhost:8080"))

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

    @GetMapping("/rateLimit")
    public Mono<String> rateLimitTest() {
        return Mono.just("\nrateLimit \n");
    }

//    @Autowired
//    RouteDefinitionLocator routeDefinitionLocator;
//
////    http://localhost:8080/actuator/gateway/routes
//    @GetMapping("/router/profile")
//    public Flux<RouteDefinition> routerProfile(  ) {
//        return routeDefinitionLocator.getRouteDefinitions();
//    }
}
