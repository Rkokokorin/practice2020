package com.tuneit.itc.commons.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.util.PortalUtil;

public class HttpUtil {
    private HttpUtil() {
        throw new IllegalStateException("This is an utility class!");
    }

    public static String getRequestParam(String paramName) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext ec = context.getExternalContext();
        String firstParam = ec.getRequestParameterMap().get(paramName);
        if (firstParam != null) {
            return firstParam;
        }
        PortletRequest req = (PortletRequest) ec.getRequest();
        HttpServletRequest httpReq = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(req));
        return httpReq.getParameter(paramName);
    }

    public static HttpServletRequest getPortletServletRequest() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        PortletRequest request = (PortletRequest) ec.getRequest();
        return PortalUtil.getHttpServletRequest(request);
    }

    public static HttpServletRequest getOriginalServletRequest() {
        return PortalUtil.getOriginalServletRequest(getPortletServletRequest());
    }

    public static HttpServletResponse getPortletServletResponse() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        PortletResponse response = (PortletResponse) ec.getResponse();
        return PortalUtil.getHttpServletResponse(response);
    }

    public static Optional<Cookie> findCookie(Cookie[] cookies, String name) {
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(cookie -> Objects.equals(cookie.getName(), name))
            .findAny();
    }
}
