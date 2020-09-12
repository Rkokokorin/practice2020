package com.tuneit.itc.zuulproxy.model.forms;

import com.tuneit.itc.zuulproxy.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleWrapper {
    private Long id;
    private String name;
    private Boolean enabled;
    private Boolean selected;

    public RoleWrapper(Role role, boolean selected){
        this.id = role.getId();
        this.name = role.getName();
        this.enabled = role.getEnabled();
        this.selected = selected;
    }
}
