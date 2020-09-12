package com.tuneit.itc.zuulproxy.model.forms;

import lombok.Data;

import java.util.List;

@Data
public class RoleForm {
    private Long id;
    private String name;
    private Boolean enabled;
    private List<RouteWrapper> routesWrappers;

}
