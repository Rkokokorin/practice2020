package com.tuneit.itc.zuulproxy;

import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import com.tuneit.itc.zuulproxy.repositories.CustomZuulRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.MBeanExportConfiguration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.List;

@Configuration
public class AppConfig {

    @Autowired
    private CustomZuulRouteRepository routeRepository;


    @Primary
    @Bean(name = "zuulConfigProperties")
    @RefreshScope
    @ConfigurationProperties("zuul")
    public ZuulProperties zuulProperties() {
        List<CustomZuulRoute> all = routeRepository.findAll();
        ZuulProperties props = new ZuulProperties();
        for (CustomZuulRoute customZuulRoute : all) {
            if (customZuulRoute.getEnabled()) {
                props.getRoutes()
                        .put(customZuulRoute.getUuid(), new ZuulProperties.ZuulRoute(customZuulRoute.getUuid(),
                                customZuulRoute.getPath(),
                                null,
                                customZuulRoute.getOutcomeUrl(),
                                true,
                                false, new HashSet<>()));
            }
        }
        return props;
    }
}