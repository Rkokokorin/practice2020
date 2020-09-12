package com.tuneit.itc.zuulproxy;


import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import com.tuneit.itc.zuulproxy.model.Role;
import com.tuneit.itc.zuulproxy.model.User;
import com.tuneit.itc.zuulproxy.repositories.CustomZuulRouteRepository;
import com.tuneit.itc.zuulproxy.repositories.RoleRepository;
import com.tuneit.itc.zuulproxy.repositories.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "admin.panel.username=admin",
        "admin.panel.pass.hash=$2a$10$sieVzqy8wvzmaRR499aM8OAKp9ShfNhmCirmB9reYunE6C6Skuhl6",
})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ZuulConfigureServiceRolesTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomZuulRouteRepository routeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testCancelCreateRole() throws Exception {
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "cancel"))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/roles"))
                .andExpect(status().isFound()
                );
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveRole() throws Exception {
        String path = "/api/v2/path";
        String outcomeUrl = "http://outcome/url";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", path)
                        .param("outcomeUrl", outcomeUrl)
                        .param("enabled", enabled + ""))
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<CustomZuulRoute> allRoutes = routeRepository.findAll();
        var route = allRoutes.get(0);

        String name = "roleName";
        boolean roleEnabled = true;
        Long routeId = route.getId();
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", name)
                        .param("enabled", roleEnabled + "")
                        .param("routesWrappers[0].id", routeId + "")
                        .param("routesWrappers[0].selected", "true"))
                .andDo(print())
                .andExpect(redirectedUrlPattern("/admin/role/view/**"))
                .andExpect(status().isFound());
        List<Role> allRoles = roleRepository.findAll();
        Assert.assertEquals(allRoles.size(), 1);
        Role role = allRoles.get(0);
        Assert.assertEquals(role.getEnabled(), roleEnabled);
        Assert.assertEquals(role.getName(), name);
        Assert.assertEquals(role.getRoutes().size(), 1);
        Assert.assertEquals(role.getRoutes().toArray(new CustomZuulRoute[1])[0].getId(), routeId);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveRoleWithDuplicateName() throws Exception {
        String name = "roleName";
        boolean roleEnabled = true;
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", name)
                        .param("enabled", roleEnabled + ""))
                .andDo(print())
                .andExpect(redirectedUrlPattern("/admin/role/view/**"))
                .andExpect(status().isFound()
                );
        List<Role> allRoles = roleRepository.findAll();
        Assert.assertEquals(allRoles.size(), 1);
        Role role = allRoles.get(0);
        Assert.assertEquals(role.getEnabled(), roleEnabled);
        Assert.assertEquals(role.getName(), name);
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", name)
                        .param("enabled", roleEnabled + ""))
                .andDo(print())
                .andExpect(status().isOk()
                );
        allRoles = roleRepository.findAll();
        Assert.assertEquals(allRoles.size(), 1);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testEditRole() throws Exception {
        String name = "roleName";
        boolean roleEnabled = true;
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", name)
                        .param("enabled", roleEnabled + ""))
                .andDo(print())
                .andExpect(redirectedUrlPattern("/admin/role/view/**"))
                .andExpect(status().isFound());
        List<Role> allRoles = roleRepository.findAll();
        Assert.assertEquals(allRoles.size(), 1);
        Role role = allRoles.get(0);
        Assert.assertEquals(role.getEnabled(), roleEnabled);
        Assert.assertEquals(role.getName(), name);

        var id = role.getId();
        String newName = "newRoleName";
        boolean newRoleEnabled = false;
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", newName)
                        .param("enabled", newRoleEnabled + "")
                        .param("id", id + ""))
                .andDo(print())
                .andExpect(redirectedUrlPattern("/admin/role/view/**"))
                .andExpect(status().isFound()
                );
        allRoles = roleRepository.findAll();
        Assert.assertEquals(allRoles.size(), 1);
        role = allRoles.get(0);
        Assert.assertEquals(role.getEnabled(), newRoleEnabled);
        Assert.assertEquals(role.getName(), newName);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testRemoveRole() throws Exception {
        String name = "roleName";
        boolean roleEnabled = true;
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", name)
                        .param("enabled", roleEnabled + ""))
                .andDo(print())
                .andExpect(redirectedUrlPattern("/admin/role/view/**"))
                .andExpect(status().isFound());
        List<Role> allRoles = roleRepository.findAll();
        Assert.assertEquals(allRoles.size(), 1);
        Role role = allRoles.get(0);

        var id = role.getId();
        this.mockMvc.perform(
                post("/admin/role/remove")
                        .with(csrf())
                        .param("action", "remove")
                        .param("force", "true")
                        .param("id", id + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/roles"))
                .andExpect(status().isFound()
                );
        allRoles = roleRepository.findAll();
        Assert.assertEquals(allRoles.size(), 0);
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testRemoveSelectedRole() throws Exception {
        String name = "roleName";
        boolean roleEnabled = true;
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", name)
                        .param("enabled", roleEnabled + ""))
                .andExpect(redirectedUrlPattern("/admin/role/view/**"))
                .andExpect(status().isFound());

        var role = roleRepository.findAll().get(0);

        String username = "username";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("enabled", enabled + "")
                        .param("rolesWrappers[0].id", role.getId() + "")
                        .param("rolesWrappers[0].selected", "true"))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound());
        List<User> all = userRepository.findAll();
        Assert.assertEquals(all.size(), 1);
        var user = all.get(0);
        Assert.assertEquals(user.getEnabled(), enabled);
        Assert.assertEquals(user.getUsername(), username);
        Assert.assertEquals(user.getRoles().size(), 1);

        this.mockMvc.perform(
                post("/admin/role/remove/")
                        .with(csrf())
                        .param("action", "remove")
                        .param("force", "false")
                        .param("id", role.getId() + ""))
                .andExpect(status().isOk());

        var allRoles = roleRepository.findAll();
        Assert.assertEquals(1, allRoles.size());

        this.mockMvc.perform(
                post("/admin/role/remove/")
                        .with(csrf())
                        .param("action", "remove")
                        .param("force", "true")
                        .param("id", role.getId() + ""))
                .andExpect(redirectedUrl("/admin/roles"))
                .andExpect(status().isFound());
        allRoles = roleRepository.findAll();
        Assert.assertEquals(0, allRoles.size());
    }
}

