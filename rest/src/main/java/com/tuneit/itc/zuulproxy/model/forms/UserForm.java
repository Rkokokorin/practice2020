package com.tuneit.itc.zuulproxy.model.forms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserForm {
    private Long id;
    private String username;
    private Boolean enabled;
    private List<RoleWrapper> rolesWrappers;
}
