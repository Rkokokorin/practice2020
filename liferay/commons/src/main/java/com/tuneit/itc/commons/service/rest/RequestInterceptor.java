package com.tuneit.itc.commons.service.rest;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RequestInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request newRequest = originalRequest.newBuilder()
            .header(RequesterProperties.getAuthHeaderName(),
                RequesterProperties.getAuthHeaderValue())
            .build();
        return chain.proceed(newRequest);
    }
}
