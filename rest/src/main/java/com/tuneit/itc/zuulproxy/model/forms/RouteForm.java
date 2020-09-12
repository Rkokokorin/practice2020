package com.tuneit.itc.zuulproxy.model.forms;

import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteForm {
    private String uuid;
    private String path;
    private String outcomeUrl;
    private boolean enabled;

    public RouteForm(CustomZuulRoute route) {
        this.uuid = route.getUuid();
        this.path = route.getPath();
        this.outcomeUrl = route.getOutcomeUrl();
        this.enabled = route.getEnabled();
    }
}