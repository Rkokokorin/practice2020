package com.tuneit.itc.catalogue.util;

import java.nio.charset.StandardCharsets;

public class URLEncoder {
    private URLEncoder() {
    }

    public static String encode(String url) {
        int fpos = url.lastIndexOf('/');
        return url.substring(0, fpos + 1)
            + java.net.URLEncoder.encode(url.substring(fpos + 1), StandardCharsets.UTF_8);
    }
}
