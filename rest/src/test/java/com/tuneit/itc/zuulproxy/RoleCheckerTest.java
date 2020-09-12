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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class RoleCheckerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomZuulRouteRepository routeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;


    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testCheckRole() throws Exception {
        String path = "/api/v2/path";
        String outcomeUrl = "https://yandex.ru";
        boolean routeEnabled = true;
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", path)
                        .param("outcomeUrl", outcomeUrl)
                        .param("enabled", routeEnabled + ""))
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );

        String anotherPath = "/api/v2/ping";
        String anotherOutcomeUrl = "https://google.com";
        boolean anotherRouteEnabled = true;
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", anotherPath)
                        .param("outcomeUrl", anotherOutcomeUrl)
                        .param("enabled", anotherRouteEnabled + ""))
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
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound());
        List<User> all = userRepository.findAll();
        Assert.assertEquals(all.size(), 1);
        var user = all.get(0);
        Assert.assertEquals(user.getEnabled(), enabled);
        Assert.assertEquals(user.getUsername(), username);
        Assert.assertEquals(user.getRoles().size(), 1);


        this.mockMvc.perform(
                get("/api/v2/ping")
                        .header("X-Proxy-Forwarded-CN", username)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        this.mockMvc.perform(
                get("/api/v2/path")
                        .header("X-Proxy-Forwarded-CN", username)
                        .with(csrf()))
                .andExpect(status().isOk());

        this.mockMvc.perform(
                get("/api/v2/path")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        boolean newRoleEnabled = false;
        this.mockMvc.perform(
                post("/admin/addRole")
                        .with(csrf())
                        .param("action", "save")
                        .param("name", name)
                        .param("enabled", newRoleEnabled + "")
                        .param("id", role.getId() + ""))
                .andDo(print())
                .andExpect(redirectedUrlPattern("/admin/role/view/**"))
                .andExpect(status().isFound()
                );

        this.mockMvc.perform(
                get("/api/v2/path")
                        .header("X-Proxy-Forwarded-CN", username)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

}

