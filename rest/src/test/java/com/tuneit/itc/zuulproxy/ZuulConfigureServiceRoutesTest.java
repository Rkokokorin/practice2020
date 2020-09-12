package com.tuneit.itc.zuulproxy;


import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import com.tuneit.itc.zuulproxy.model.Role;
import com.tuneit.itc.zuulproxy.repositories.CustomZuulRouteRepository;
import com.tuneit.itc.zuulproxy.repositories.RoleRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
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
public class ZuulConfigureServiceRoutesTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomZuulRouteRepository routeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ZuulProperties zuulProperties;

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testCancelCreateRoute() throws Exception {
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "cancel"))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveCreateRoute() throws Exception {
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
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<CustomZuulRoute> all = routeRepository.findAll();
        Assert.assertEquals(1, all.size());
        var route = all.get(0);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(path, route.getPath());
        Assert.assertEquals(outcomeUrl, route.getOutcomeUrl());
        Assert.assertNotNull(zuulProperties.getRoutes().get(route.getUuid()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveCreateRouteEmptyUrls() throws Exception {
        String path = "";
        String outcomeUrl = "";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", path)
                        .param("outcomeUrl", outcomeUrl)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(status().isOk()
                );
        List<CustomZuulRoute> all = routeRepository.findAll();
        Assert.assertEquals(0, all.size());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testEditRoute() throws Exception {
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
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<CustomZuulRoute> all = routeRepository.findAll();
        Assert.assertEquals(1, all.size());
        var route = all.get(0);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(path, route.getPath());
        Assert.assertEquals(outcomeUrl, route.getOutcomeUrl());
        var uuid = route.getUuid();
        Assert.assertNotNull(zuulProperties.getRoutes().get(route.getUuid()));
        String newPath = "/api/v2/new";
        String newOutcomeUrl = "http://outcome/url/new";
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", newPath)
                        .param("outcomeUrl", newOutcomeUrl)
                        .param("uuid", uuid)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        route = routeRepository.findByUuid(uuid);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(newPath, route.getPath());
        Assert.assertEquals(newOutcomeUrl, route.getOutcomeUrl());
        Assert.assertNotNull(zuulProperties.getRoutes().get(route.getUuid()));
        Assert.assertEquals(newPath, zuulProperties.getRoutes().get(route.getUuid()).getPath());
        Assert.assertEquals(newOutcomeUrl, zuulProperties.getRoutes().get(route.getUuid()).getUrl());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testDisableRoute() throws Exception {
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
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<CustomZuulRoute> all = routeRepository.findAll();
        Assert.assertEquals(1, all.size());
        var route = all.get(0);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(path, route.getPath());
        Assert.assertEquals(outcomeUrl, route.getOutcomeUrl());
        var uuid = route.getUuid();
        Assert.assertNotNull(zuulProperties.getRoutes().get(route.getUuid()));

        enabled = false;
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", path)
                        .param("outcomeUrl", outcomeUrl)
                        .param("uuid", uuid)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        route = routeRepository.findByUuid(uuid);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(path, route.getPath());
        Assert.assertEquals(outcomeUrl, route.getOutcomeUrl());
        Assert.assertNull(zuulProperties.getRoutes().get(route.getUuid()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveRouteWithExistingPath() throws Exception {
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
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<CustomZuulRoute> all = routeRepository.findAll();
        Assert.assertEquals(1, all.size());
        var route = all.get(0);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(path, route.getPath());
        Assert.assertEquals(outcomeUrl, route.getOutcomeUrl());
        String newOutcomeUrl = outcomeUrl + "newOutcome";

        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", path)
                        .param("outcomeUrl", newOutcomeUrl)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(status().isOk()
                );
        all = routeRepository.findAll();
        Assert.assertEquals(1, all.size());
        route = all.get(0);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(path, route.getPath());
        Assert.assertEquals(outcomeUrl, route.getOutcomeUrl());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testSaveRouteWithAdminPrefix() throws Exception {
        String path = "/admin/path";
        String outcomeUrl = "http://outcome/url";
        boolean enabled = true;
        this.mockMvc.perform(
                post("/admin/addRoute")
                        .with(csrf())
                        .param("action", "save")
                        .param("path", path)
                        .param("outcomeUrl", outcomeUrl)
                        .param("enabled", enabled + ""))
                .andDo(print())
                .andExpect(status().isOk()
                );
        List<CustomZuulRoute> all = routeRepository.findAll();
        Assert.assertEquals(0, all.size());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testRemoveRoute() throws Exception {
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
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        List<CustomZuulRoute> all = routeRepository.findAll();
        Assert.assertEquals(1, all.size());
        var route = all.get(0);
        Assert.assertEquals(enabled, route.getEnabled());
        Assert.assertEquals(path, route.getPath());
        Assert.assertEquals(outcomeUrl, route.getOutcomeUrl());
        Assert.assertNotNull(zuulProperties.getRoutes().get(route.getUuid()));

        this.mockMvc.perform(
                post("/admin/route/remove/")
                        .with(csrf())
                        .param("action", "remove")
                        .param("force", "true")
                        .param("uuid", route.getUuid()))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound()
                );
        all = routeRepository.findAll();
        Assert.assertEquals(0, all.size());
        Assert.assertNull(zuulProperties.getRoutes().get(route.getUuid()));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void testRemoveSelectedRoute() throws Exception {
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
        Assert.assertEquals(1, allRoles.size());

        this.mockMvc.perform(
                post("/admin/route/remove/")
                        .with(csrf())
                        .param("action", "remove")
                        .param("force", "false")
                        .param("uuid", route.getUuid()))
                .andDo(print())
                .andExpect(status().isOk());

        allRoutes = routeRepository.findAll();
        Assert.assertEquals(1, allRoutes.size());

        this.mockMvc.perform(
                post("/admin/route/remove/")
                        .with(csrf())
                        .param("action", "remove")
                        .param("force", "true")
                        .param("uuid", route.getUuid()))
                .andDo(print())
                .andExpect(redirectedUrl("/admin/routes"))
                .andExpect(status().isFound());
        allRoutes = routeRepository.findAll();
        Assert.assertEquals(0, allRoutes.size());
    }

}

