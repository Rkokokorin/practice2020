package com.tuneit.itc.zuulproxy;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.tuneit.itc.zuulproxy.model.ApiUser;
import com.tuneit.itc.zuulproxy.repositories.ApiUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class BasicAuthFilter extends ZuulFilter {

    @Autowired
    private ApiUserRepository apiUserRepository;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        List<ApiUser> all = apiUserRepository.findAll();
        String username = "username";
        String password = "password";
        if (!all.isEmpty()) {
            ApiUser apiUser = all.get(0);
            username = apiUser.getUsername();
            password = apiUser.getPassword();
        }
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.getRequest().getRequestURL();
        ctx.addZuulRequestHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        return null;
    }

}