package com.tuneit.itc.zuulproxy;


import com.tuneit.itc.zuulproxy.model.ApiUser;
import com.tuneit.itc.zuulproxy.repositories.ApiUserRepository;
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

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
public class ZuulConfigureServiceApiUsersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiUserRepository apiUserRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testCancelCreateUser() throws Exception {
        this.mockMvc.perform(
                post("/admin/addAPIUser")
                        .with(csrf())
                        .param("action", "cancel"))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveCreateUser() throws Exception {
        String username = "user";
        String password = "pass";
        String confirmPassword = "pass";
        this.mockMvc.perform(
                post("/admin/addAPIUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("password", password)
                        .param("confirmPassword", confirmPassword))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<ApiUser> all = apiUserRepository.findAll();
        Assert.assertEquals(all.size(), 1);
        var user = all.get(0);
        Assert.assertEquals(user.getUsername(), username);
        Assert.assertEquals(user.getPassword(), password);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveCreateUserWrongPassword() throws Exception {
        String username = "user";
        String password = "pass";
        String confirmPassword = "sss";
        this.mockMvc.perform(
                post("/admin/addAPIUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("password", password)
                        .param("confirmPassword", confirmPassword))
                .andDo(print())
                .andExpect(status().isOk()
                );
        List<ApiUser> all = apiUserRepository.findAll();
        Assert.assertEquals(all.size(), 0);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testEditUser() throws Exception {
        String username = "user";
        String password = "pass";
        String confirmPassword = "pass";
        this.mockMvc.perform(
                post("/admin/addAPIUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", username)
                        .param("password", password)
                        .param("confirmPassword", confirmPassword))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<ApiUser> all = apiUserRepository.findAll();
        Assert.assertEquals(all.size(), 1);
        var user = all.get(0);
        String newUsername = "uss";
        String newPassword = "pwd";
        String newConfirmPassword = "pwd";
        this.mockMvc.perform(
                post("/admin/addAPIUser")
                        .with(csrf())
                        .param("action", "save")
                        .param("username", newUsername)
                        .param("password", newPassword)
                        .param("confirmPassword", newConfirmPassword))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        user = apiUserRepository.findByUsername(newUsername);
        Assert.assertNotNull(user);
        Assert.assertEquals("New username is not equal", user.getUsername(), newUsername);
        Assert.assertEquals(user.getPassword(), newPassword);
    }
}

