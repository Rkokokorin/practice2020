package com.tuneit.itc.zuulproxy;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.tuneit.itc.zuulproxy.model.Preferences;
import com.tuneit.itc.zuulproxy.model.User;
import com.tuneit.itc.zuulproxy.repositories.PreferencesRepository;
import com.tuneit.itc.zuulproxy.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

@Component
@Slf4j
public class RoleCheckerFilter extends ZuulFilter {
    private final String ROUTE_HOST_KEY = "routeHost";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PreferencesRepository preferencesRepository;

    @Override
    public Object run() {
        Preferences preferences = preferencesRepository.findAll().get(0);
        final RequestContext requestContext = RequestContext.getCurrentContext();
        final HttpServletRequest req = requestContext.getRequest();
        String username = requestContext.getRequest().getHeader(preferences.getAuthHeader());
        if (username == null) {
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.setResponseBody("No authorization present");
            log.error("Unauthorized request to {}. No authorization present", req.getRequestURL());
            return null;
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.setResponseBody("User not found");
            log.error("Unauthorized request to {}. No such user for username {}", req.getRequestURL(), username);
            return null;
        }

        if (!user.getEnabled()) {
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.setResponseBody("The given user is deactivated");
            log.error("Unauthorized request to {}. User {} with id {} is deactivated",
                    req.getRequestURL(), user.getUsername(), user.getId());
            return null;
        }
        URL requestedHost = (URL) requestContext.get(ROUTE_HOST_KEY);

        boolean permitted = userRepository.checkPermissionForUserAndHost(user.getId(), requestedHost.toExternalForm());
        if (!permitted) {
            log.error("Unauthorized request from {} to {}. User doesn't have an appropriate role", user.getUsername(), req.getRequestURL());
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.setResponseBody("The given user doesn't have an appropriate role");
            return null;
        }
        log.info("Authorized request from {} to {}", user.getUsername(), req.getRequestURL());
        return null;
    }


    @Override
    public boolean shouldFilter() {
        Preferences preferences = preferencesRepository.findAll().get(0);
        return preferences.getAuthEnabled();
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
}