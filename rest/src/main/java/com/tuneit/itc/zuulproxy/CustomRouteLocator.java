package com.tuneit.itc.zuulproxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Pashnin
 * <p>
 * Custom route locator for strict path matching and routing on target URL root
 */
@Slf4j
public class CustomRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator {
    private ZuulProperties properties;

    public CustomRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
        this.properties = properties;
    }

    /**
     * Sets route's target path to empty string.
     * It is used for routing strictly on target URL root
     *
     * @param route - matched route
     * @param path  - path for route preprocessing
     * @return route for request
     */
    @Override
    protected Route getRoute(ZuulProperties.ZuulRoute route, String path) {
        Route r = super.getRoute(route, path);
        if (r != null) {
            if(r.getPath().equals(r.getFullPath())){
                r.setPath("");
            }
        }
        return r;
    }

    /**
     * Do the same as {@link SimpleRouteLocator}, but uses routeMap's keys as path
     *
     * @return look here {@link SimpleRouteLocator}.
     */
    @Override
    public List<Route> getRoutes() {
        List<Route> values = new ArrayList<>();
        for (Map.Entry<String, ZuulProperties.ZuulRoute> entry : getRoutesMap().entrySet()) {
            ZuulProperties.ZuulRoute route = entry.getValue();
            String path = entry.getKey();
            try {
                values.add(getRoute(route, path));
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Invalid route, routeId: " + route.getId()
                            + ", routeServiceId: " + route.getServiceId() + ", msg: "
                            + e.getMessage());
                }
                if (log.isDebugEnabled()) {
                    log.debug("", e);
                }
            }
        }
        return values;
    }

    /**
     * As we want our RouteLocator to process path with trailing slash we maps each route on /url and /url/
     *
     * @return look here {@link SimpleRouteLocator}.
     */
    @Override
    protected Map<String, ZuulProperties.ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulProperties.ZuulRoute> routesMap = new LinkedHashMap<>();
        for (ZuulProperties.ZuulRoute route : this.properties.getRoutes().values()) {
            routesMap.put(route.getPath(), route);
            routesMap.put(route.getPath() + "/", route);
        }
        return routesMap;
    }

    /**
     * Make RouteLocator refreshable for dynamic routing
     */
    @Override
    public void refresh() {
        doRefresh();
    }
}
