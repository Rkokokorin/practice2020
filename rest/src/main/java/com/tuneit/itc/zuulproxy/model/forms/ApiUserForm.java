package com.tuneit.itc.zuulproxy.model.forms;

import lombok.Data;

@Data
public class ApiUserForm {
    private String username;
    private String password;
    private String confirmPassword;
}
