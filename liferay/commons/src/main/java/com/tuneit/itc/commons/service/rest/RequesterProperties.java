package com.tuneit.itc.commons.service.rest;

import com.tuneit.itc.commons.util.Properties;

/**
 * Properties for ITC service requester.
 *
 * @author kk
 */
public class RequesterProperties extends Properties {
    private static RequesterProperties instance = null;
    private String schema = "http://";
    private String host = "localhost";
    private int port = 8181;
    private String baseUrl = null;
    private String authHeaderName = "X-Proxy-Forwarded-CN";
    private String authHeaderValue = "Portal";

    private RequesterProperties() {
        init();
    }

    private static RequesterProperties getInstance() {
        if (null == instance) {
            instance = new RequesterProperties();
        }
        return instance;
    }

    public static String getSchema() {
        return getInstance().schema;
    }

    public static String getHost() {
        return getInstance().host;
    }

    public static int getPort() {
        return getInstance().port;
    }

    public static String getBaseUrl() {
        return getInstance().baseUrl;
    }

    public static String getAuthHeaderName() {
        return getInstance().authHeaderName;
    }

    public static String getAuthHeaderValue() {
        return getInstance().authHeaderValue;
    }

    private void init() {
        if (null != baseUrl) {
            return;
        }
        schema = getOrDefault("requester.schema", schema);
        host = getOrDefault("requester.host", host);
        port = getOrDefault("requester.port", port);
        authHeaderName = getOrDefault("requester.auth.header.name",
            authHeaderName);
        authHeaderValue = getOrDefault("requester.auth.header.value",
            authHeaderValue);

        baseUrl = schema + host + ':' + port;
    }
}
