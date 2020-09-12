package com.tuneit.itc.zuulproxy;


import com.tuneit.itc.zuulproxy.repositories.CustomZuulRouteRepository;
import com.tuneit.itc.zuulproxy.repositories.ApiUserRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class ZuulConfigureServiceCommonTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomZuulRouteRepository routeRepository;
    @Autowired
    private ApiUserRepository apiUserRepository;

    @Test
    public void testLoginRedirection() throws Exception {
        this.mockMvc.perform(get("/admin/routes")).andDo(print())
                .andExpect(redirectedUrl("http://localhost/login")).andExpect(status().isFound());
    }

    @Test
    public void testLogin() throws Exception {
        this.mockMvc.perform(formLogin().user("admin").password("admin")).andDo(print())
                .andExpect(redirectedUrl("/admin/console")).andExpect(status().isFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testGetRoutesPage() throws Exception {
        this.mockMvc.perform(get("/admin/routes")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testGetAddRoutePage() throws Exception {
        this.mockMvc.perform(get("/admin/addRoute")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testGetAddAPIUserPage() throws Exception {
        this.mockMvc.perform(get("/admin/addAPIUser")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testGetRolesPage() throws Exception {
        this.mockMvc.perform(get("/admin/roles")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testGetAddRolePage() throws Exception {
        this.mockMvc.perform(get("/admin/addRole")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testGetUsersPage() throws Exception {
        this.mockMvc.perform(get("/admin/users")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testGetAddUsersPage() throws Exception {
        this.mockMvc.perform(get("/admin/addUser")).andDo(print()).andExpect(status().isOk());
    }
}

