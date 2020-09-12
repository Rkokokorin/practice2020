package com.tuneit.itc.zuulproxy;


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
public class ZuulConfigureServiceUsersTest {

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
    public void testCancelCreateUser() throws Exception {
        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "cancel"))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound()
                );
    }

    @Test
    @Transactional
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveUser() throws Exception {
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
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveUserEmptyUsername() throws Exception {
        String username = "";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(status().isOk());
        List<User> all = userRepository.findAll();
        Assert.assertEquals(0, all.size());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testEditUser() throws Exception {
        String username = "username";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound());
        List<User> all = userRepository.findAll();
        Assert.assertEquals(1, all.size());
        var user = all.get(0);
        Assert.assertEquals(enabled, user.getEnabled());
        Assert.assertEquals(username, user.getUsername());

        String newUsername = "newusername";
        boolean newEnabled = false;
        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", newUsername)
                        .param("id", user.getId()+"")
                        .param("enabled", newEnabled + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound());
        all = userRepository.findAll();
        Assert.assertEquals(1, all.size());
        user = all.get(0);
        Assert.assertEquals(newEnabled, user.getEnabled());
        Assert.assertEquals(newUsername, user.getUsername());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveUserWithExistingUsername() throws Exception {
        String username = "username";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound());
        List<User> all = userRepository.findAll();
        Assert.assertEquals(all.size(), 1);
        var user = all.get(0);
        Assert.assertEquals(enabled, user.getEnabled());
        Assert.assertEquals(username, user.getUsername());

        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(status().isOk());
        all = userRepository.findAll();
        Assert.assertEquals(all.size(), 1);
        user = all.get(0);
        Assert.assertEquals(enabled, user.getEnabled());
        Assert.assertEquals(username, user.getUsername());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testRemoveUser() throws Exception {
        String username = "username";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound());
        List<User> all = userRepository.findAll();
        Assert.assertEquals(all.size(), 1);
        var user = all.get(0);
        Assert.assertEquals(enabled, user.getEnabled());
        Assert.assertEquals(username, user.getUsername());

        this.mockMvc.perform(
                post("/admin/user/remove/")
                        .with(csrf())
                        .param("action", "remove")
                        .param("force", "true")
                        .param("id", user.getId() + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(status().isFound()
                );
        all = userRepository.findAll();
        Assert.assertEquals(0, all.size());
    }

}

