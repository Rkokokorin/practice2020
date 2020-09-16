package com.tuneit.itc.contacts.util;

import lombok.Data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Data
public class RequestParam {
    private final String name;
    private final String value;

    public String toUrlParam() {
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        return encodedName + "=" + encodedValue;
    }
}
