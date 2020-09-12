package com.tuneit.itc.zuulproxy.metrics.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.net.URL;
import java.time.Instant;

import com.tuneit.itc.zuulproxy.metrics.beans.TimeMetrics;
import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import com.tuneit.itc.zuulproxy.repositories.CustomZuulRouteRepository;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;

@Slf4j
@Component
public class TimeMetricsPostFilter extends ZuulFilter {
    public static final String START_TIME_ATTR = "com.tuneit.itc.startTime";
    @Autowired
    private TimeMetrics timeMetrics;
    @Autowired
    private ZuulProperties zuulProperties;

    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_RESPONSE_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        Object startTime = request.getAttribute(START_TIME_ATTR);
        String redirectUrl = context.get("routeHost").toString();
        String proxy = context.get("proxy").toString();
        String requestURI = context.get("requestURI").toString();
        ZuulProperties.ZuulRoute zuulRoute = zuulProperties.getRoutes().get(proxy);
        timeMetrics.add(zuulRoute.getPath(), redirectUrl + requestURI, (Instant)startTime, Instant.now());
        return null;
    }
}
