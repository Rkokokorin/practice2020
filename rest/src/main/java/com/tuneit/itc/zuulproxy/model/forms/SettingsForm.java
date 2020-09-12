package com.tuneit.itc.zuulproxy.model.forms;

import lombok.Data;

@Data
public class SettingsForm {
    private String authHeader;
    private Boolean authEnabled;
}
