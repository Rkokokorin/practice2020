package com.tuneit.itc.zuulproxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZuulProxy
@Slf4j
public class ZuulProxyApplication {
    @Autowired
    protected ZuulProperties zuulProperties;
    @Autowired
    protected ServerProperties server;


    public static void main(String[] args) {
        SpringApplication.run(ZuulProxyApplication.class, args);
    }

    @Bean
    public CustomRouteLocator customRouteLocator() {
        return new CustomRouteLocator(server.getServlet().getContextPath(), zuulProperties);
    }
}
