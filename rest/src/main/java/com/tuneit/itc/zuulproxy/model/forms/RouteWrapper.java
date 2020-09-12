package com.tuneit.itc.zuulproxy.model.forms;

import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteWrapper {
    private Long id;
    private String path;
    private String outcomeUrl;
    private Boolean enabled;
    private Boolean selected;

    public RouteWrapper(CustomZuulRoute route, boolean selected){
        this.id = route.getId();
        this.path = route.getPath();
        this.outcomeUrl = route.getOutcomeUrl();
        this.enabled = route.getEnabled();
        this.selected = selected;
    }
}
