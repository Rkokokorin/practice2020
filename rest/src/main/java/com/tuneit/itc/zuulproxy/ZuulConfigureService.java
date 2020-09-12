package com.tuneit.itc.zuulproxy;

import com.tuneit.itc.zuulproxy.model.ApiUser;
import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import com.tuneit.itc.zuulproxy.model.Preferences;
import com.tuneit.itc.zuulproxy.model.Role;
import com.tuneit.itc.zuulproxy.model.User;
import com.tuneit.itc.zuulproxy.model.forms.ApiUserForm;
import com.tuneit.itc.zuulproxy.model.forms.RoleForm;
import com.tuneit.itc.zuulproxy.model.forms.RoleWrapper;
import com.tuneit.itc.zuulproxy.model.forms.RouteForm;
import com.tuneit.itc.zuulproxy.model.forms.RouteWrapper;
import com.tuneit.itc.zuulproxy.model.forms.SettingsForm;
import com.tuneit.itc.zuulproxy.model.forms.UserForm;
import com.tuneit.itc.zuulproxy.repositories.CustomZuulRouteRepository;
import com.tuneit.itc.zuulproxy.repositories.PreferencesRepository;
import com.tuneit.itc.zuulproxy.repositories.RoleRepository;
import com.tuneit.itc.zuulproxy.repositories.ApiUserRepository;
import com.tuneit.itc.zuulproxy.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Alexaner Pashnin
 */
@Component
@RequestMapping("/admin")
@Slf4j
public class ZuulConfigureService {

    @Autowired
    private ZuulHandlerMapping zuulHandlerMapping;
    @Autowired
    private ZuulProperties zuulProperties;
    @Autowired
    private CustomZuulRouteRepository routeRepository;
    @Autowired
    private ApiUserRepository apiUserRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PreferencesRepository preferencesRepository;

    /**
     * Returns page routes.html with table of existed routes
     *
     * @param model - thymeleaf view model
     * @return page routes.html with table of existed routes
     */
    @GetMapping(value = {"/routes", "/console"})
    public String routesList(Model model) {
        loadRoutesPageData(model);
        return "routes";
    }

    private void loadRoutesPageData(Model model) {
        List<CustomZuulRoute> routes = routeRepository.findAll();
        List<ApiUser> all = apiUserRepository.findAll();
        List<Preferences> prefs = preferencesRepository.findAll();
        String authHeader = "Ошибка загрузки";
        Boolean authEnabled = true;
        if (!prefs.isEmpty()) {
            Preferences preferences = prefs.get(0);
            authHeader = preferences.getAuthHeader();
            authEnabled = preferences.getAuthEnabled();
        }
        String username = null;
        if (!all.isEmpty()) {
            username = all.get(0).getUsername();
        }
        model.addAttribute("authHeader", authHeader);
        model.addAttribute("authEnabled", authEnabled);
        model.addAttribute("routes", routes);
        model.addAttribute("username", username);
    }

    /**
     * Returns page addAPIUser.html for creating a new APIUser
     *
     * @param model - thymeleaf view model
     * @return page addAPIUser.html for creating a new APIUser
     */
    @GetMapping("/addAPIUser")
    public String showaddAPIUserForm(Model model) {
        ApiUserForm apiUserForm = new ApiUserForm();
        model.addAttribute("userForm", apiUserForm);
        return "addAPIUser";
    }

    /**
     * @param model    - thymeleaf view model
     * @param username - username of APIUser to edit
     * @return page addAPIUser.html with pre-loaded data for editing
     */
    @GetMapping("/api/user/edit/{username}")
    public String showaddAPIUserForm(Model model, @PathVariable(value = "username") String username) {
        ApiUserForm apiUserForm = new ApiUserForm();
        ApiUser first = apiUserRepository.findByUsername(username);
        apiUserForm.setUsername(first.getUsername());
        model.addAttribute("userForm", apiUserForm);
        return "addAPIUser";
    }

    /**
     * Saves APIUser (new, or edited)
     *
     * @param model       - thymeleaf view model
     * @param apiUserForm - object that contains data for new user
     * @return redirect to routes.html
     */
    @PostMapping(value = "/addAPIUser", params = "action=save")
    public String saveUser(Model model,
                           @ModelAttribute("userForm") ApiUserForm apiUserForm) {
        List<ApiUser> all = apiUserRepository.findAll();
        ApiUser apiUser = new ApiUser();
        if (!all.isEmpty()) {
            apiUser = all.get(0);
        }
        if (apiUserForm.getPassword() != null
                && !apiUserForm.getPassword().isBlank()
                && apiUserForm.getPassword().equals(apiUserForm.getConfirmPassword())) {
            apiUser.setUsername(apiUserForm.getUsername());
            apiUser.setPassword(apiUserForm.getPassword());
            apiUserRepository.save(apiUser);
        } else {
            model.addAttribute("errorMessage", "Пароли не совпадают");
            return "addAPIUser";
        }
        return "redirect:/admin/routes";
    }

    /**
     * Cancel editing APIUser
     *
     * @return redirect to routes.html
     */
    @PostMapping(value = "/addAPIUser", params = "action=cancel")
    public String canceladdAPIUser() {
        return "redirect:/admin/routes";
    }

    /**
     * @param model - thymeleaf view model
     * @return page addRoute.html
     */
    @GetMapping("/addRoute")
    public String showAddRouteForm(Model model) {

        RouteForm routeForm = new RouteForm();
        routeForm.setEnabled(true);
        model.addAttribute("routeForm", routeForm);

        return "addRoute";
    }

    /**
     * @param model - thymeleaf view model
     * @param uuid  - uuid of edited route
     * @return page addRoute.html with pre-loaded data for editing
     */
    @GetMapping("/route/edit/{uuid}")
    public String showAddRouteForm(Model model, @PathVariable(value = "uuid") String uuid) {
        RouteForm routeForm = new RouteForm();
        CustomZuulRoute first = routeRepository.findByUuid(uuid);
        routeForm.setOutcomeUrl(first.getOutcomeUrl());
        routeForm.setPath(first.getPath());
        routeForm.setUuid(first.getUuid());
        routeForm.setEnabled(first.getEnabled());
        model.addAttribute("routeForm", routeForm);
        log.info("Go to editing route {}", first);
        return "addRoute";
    }

    /**
     * Saves route (new or edited) and updates zuulProperties
     *
     * @param model     - thymeleaf view model
     * @param routeForm - contains data for route
     * @return redirect to routes.html or errorMessage
     */
    @PostMapping(value = "/addRoute", params = "action=save")
    public String saveRoute(Model model,
                            @ModelAttribute("routeForm") RouteForm routeForm) {

        String path = routeForm.getPath();
        String outcomeUrl = routeForm.getOutcomeUrl();

        if (path != null && path.length() > 0 //
                && outcomeUrl != null && outcomeUrl.length() > 0) {
            if (path.startsWith("/admin/")) {
                log.error("Creating route failed {}. Path can't start with /admin/", routeForm);
                model.addAttribute("errorMessage", "Внешний УРЛ не может начинаться с префикса /admin/");
                return "addRoute";
            }
            if (routeForm.getUuid() == null || routeForm.getUuid().isBlank()) {
                String uuid = UUID.randomUUID().toString();
                CustomZuulRoute findByPath = routeRepository.findByPath(path);
                if (findByPath != null) {
                    log.error("Creating route failed {}. Path already exists", routeForm);
                    model.addAttribute("errorMessage", "Сервис с таким внешним УРЛ уже существует");
                    return "addRoute";
                }
                CustomZuulRoute newRoute = new CustomZuulRoute(uuid, path.strip(), outcomeUrl.strip(), routeForm.isEnabled());
                routeRepository.save(newRoute);
                log.info("Create new route {}", newRoute);
                zuulProperties.getRoutes().put(uuid, new ZuulProperties.ZuulRoute(uuid, newRoute.getPath(), null, newRoute.getOutcomeUrl(), true, false, new HashSet<>()));
                zuulHandlerMapping.setDirty(true);
                return "redirect:/admin/routes";
            } else {
                CustomZuulRoute findByPath = routeRepository.findByPath(path);
                if (findByPath != null && !findByPath.getUuid().equals(routeForm.getUuid())) {
                    log.error("Creating route failed {}. Path already exists", routeForm);
                    model.addAttribute("errorMessage", "Сервис с таким внешним УРЛ уже существует");
                    return "addRoute";
                }
                CustomZuulRoute first = routeRepository.findByUuid(routeForm.getUuid());
                first.setPath(routeForm.getPath().strip());
                first.setEnabled(routeForm.isEnabled());
                first.setOutcomeUrl(routeForm.getOutcomeUrl().strip());
                if (routeForm.isEnabled()) {
                    zuulProperties.getRoutes()
                            .put(first.getUuid(), new ZuulProperties.ZuulRoute(first.getUuid(), first.getPath(), null, first.getOutcomeUrl(), true, false, new HashSet<>()));
                } else {
                    zuulProperties.getRoutes().remove(first.getUuid());
                }
                log.info("Edited route {}", first);
                routeRepository.save(first);
                zuulHandlerMapping.setDirty(true);
                return "redirect:/admin/routes";
            }

        }
        log.error("Creating route failed {}. Path our outcome are empty", routeForm);
        model.addAttribute("errorMessage", "Внешний и внутренний УРЛ не могут быть пусты");
        return "addRoute";
    }

    /**
     * Cancel editing route
     *
     * @return redirect to routes.html
     */
    @PostMapping(value = "/addRoute", params = "action=cancel")
    public String cancelAddRoute() {
        return "redirect:/admin/routes";
    }

    /**
     * Removes route from database and zuulProperties
     *
     * @param model - thymeleaf view model
     * @param uuid  - uuid of route to remove
     * @param force - if true removes route from associated roles
     * @return redirect to routes.html
     */
    @Transactional
    @PostMapping(value = "/route/remove", params = "action=remove")
    public String removeRoute(Model model,
                              @ModelAttribute("uuid") String uuid, @ModelAttribute("force") Boolean force) {
        log.info("Remove route by uuid {} with force: {}", uuid, force);
        var route = routeRepository.findByUuid(uuid);
        if (route == null) {
            return "redirect:/admin/routes";
        }

        List<Role> roles = roleRepository.findAllBySelectedRouteId(route.getId());


        if (!force) {

            if (roles.isEmpty()) {
                String confirmMsgTemplate = "Подтвердите удаление сервиса: " + route.getPath();
                model.addAttribute("errorMessage", confirmMsgTemplate);
            } else {
                List<String> rolesNames = roles.stream().map(Role::getName).collect(Collectors.toList());
                String confirmMsgTemplate = "Данный сервис " + route.getPath() + " используется в следующих ролях: " + rolesNames + ". Действительно удалить?";
                model.addAttribute("errorMessage", confirmMsgTemplate);
            }

            loadRoutesPageData(model);
            return "routes";
        }

        if (roles.isEmpty()) {
            routeRepository.deleteById(route.getId());
            zuulProperties.getRoutes().remove(uuid);
            zuulHandlerMapping.setDirty(true);
            log.info("Route with uuid {} removed with force: {}", uuid, force);
            return "redirect:/admin/routes";
        }

        for (Role role : roles) {
            role.getRoutes().remove(route);
            log.info("Remove route with id {} from role with name {} and id {}", route.getId(), role.getName(), role.getId());
            roleRepository.save(role);
        }

        routeRepository.deleteById(route.getId());
        zuulProperties.getRoutes().remove(uuid);
        zuulHandlerMapping.setDirty(true);
        return "redirect:/admin/routes";
    }

    /**
     * @param model - thymeleaf view model
     * @return roles.html with list of all roles
     */
    @GetMapping(value = "/roles")
    public String rolesList(Model model) {
        var roles = roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "roles";
    }

    /**
     * @param model - thymeleaf view model
     * @return addRole.html with form for role creation
     */
    @GetMapping(value = "/addRole")
    public String showAddRoleForm(Model model) {
        var availableRoutes = routeRepository.findAll();
        List<RouteWrapper> routeForms = availableRoutes
                .stream()
                .sorted(Comparator.comparing(CustomZuulRoute::getPath))
                .map(r -> new RouteWrapper(r, false))
                .collect(Collectors.toList());
        RoleForm roleForm = new RoleForm();
        roleForm.setRoutesWrappers(routeForms);
        roleForm.setEnabled(true);
        model.addAttribute("roleForm", roleForm);
        return "addRole";
    }

    /**
     * Accepts role data and creates new role.
     *
     * @param model    - thymeleaf view model
     * @param roleForm - object with data for new role
     * @return redirect to view role page role.html or error message
     */
    @PostMapping(value = "/addRole", params = "action=save")
    public String saveRole(Model model, @ModelAttribute("roleForm") RoleForm roleForm) {
        if (roleForm.getName() == null || roleForm.getName().isBlank()) {
            log.error("Creating role failed {}. Name is empty", roleForm);
            model.addAttribute("errorMessage", "Имя не может быть пустым");
            return "addRole";
        }
        Role role = null;
        Role roleByName = roleRepository.findByName(roleForm.getName());
        if (roleForm.getId() == null) {
            if (roleByName != null) {
                log.error("Creating role failed {}. Name is busy", roleForm);
                model.addAttribute("errorMessage", "Роль с таким именем уже существует");
                return "addRole";
            }
            log.info("Create new role {}", roleForm);
            role = new Role();
        } else {
            Optional<Role> roleOpt = roleRepository.findById(roleForm.getId());
            if (roleOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Редактируемая роль была удалена.");
                return "addRole";
            }
            role = roleOpt.get();
            if (roleByName != null && !roleByName.getId().equals(role.getId())) {
                log.error("Creating role failed {}. Name is busy", roleForm);
                model.addAttribute("errorMessage", "Роль с таким именем уже существует");
                return "addRole";
            }
        }

        role.setEnabled(roleForm.getEnabled());
        role.setName(roleForm.getName());
        if (roleForm.getRoutesWrappers() != null && roleForm.getRoutesWrappers().size() > 0) {
            Set<CustomZuulRoute> selectedRoutes = new HashSet<>(routeRepository.findAllById(roleForm.getRoutesWrappers()
                    .stream()
                    .filter(RouteWrapper::getSelected)
                    .map(RouteWrapper::getId)
                    .collect(Collectors.toList())));
            role.setRoutes(selectedRoutes);
        }

        roleRepository.save(role);
        log.info("Saved role {}", role);
        return "redirect:/admin/role/view/" + role.getId();
    }

    /**
     * @param model - thymeleaf view model
     * @param id    - id of role to view
     * @return addRole.html with pre-loaded data to edit
     */
    @GetMapping("/role/edit/{id}")
    public String goToEditRole(Model model, @PathVariable(value = "id") Long id) {
        RoleForm roleForm = new RoleForm();
        var roleOptional = roleRepository.findById(id);
        if (roleOptional.isEmpty()) {
            log.error("Try to edit non-existing role with id {}", id);
            return "redirect:/admin/roles";
        }
        Role role = roleOptional.get();
        roleForm.setEnabled(role.getEnabled());
        roleForm.setId(role.getId());
        roleForm.setName(role.getName());
        List<RouteWrapper> selectedWrappers = roleRepository.findSelectedRouteWrappersByRoleId(role.getId());
        List<RouteWrapper> notSelectedWrappers = routeRepository.findNotSelectedRouteWrappersByRoleId(role.getId());
        List<RouteWrapper> wrappers = new ArrayList<>(selectedWrappers);
        wrappers.addAll(notSelectedWrappers);
        wrappers.sort(Comparator.comparing(RouteWrapper::getPath));
        roleForm.setRoutesWrappers(wrappers);
        roleForm.setEnabled(role.getEnabled());
        model.addAttribute("roleForm", roleForm);
        log.info("Go to editing role with name {} and id {}", role.getName(), role.getId());
        return "addRole";
    }

    /**
     * @param model - thymeleaf view model
     * @param id    - id of role to view
     * @return role.html with role to view
     */
    @GetMapping("/role/view/{id}")
    public String goToViewRole(Model model, @PathVariable(value = "id") Long id) {
        RoleForm roleForm = new RoleForm();
        var roleOptional = roleRepository.findById(id);
        if (roleOptional.isEmpty()) {
            log.error("Try to view non-existing role with id {}", id);
            return "roles";
        }
        Role role = roleOptional.get();
        roleForm.setEnabled(role.getEnabled());
        roleForm.setId(role.getId());
        roleForm.setName(role.getName());
        List<RouteWrapper> selectedWrappers = roleRepository.findSelectedRouteWrappersByRoleId(role.getId());
        List<RouteWrapper> wrappers = new ArrayList<>(selectedWrappers);
        wrappers.sort(Comparator.comparing(RouteWrapper::getPath));
        roleForm.setRoutesWrappers(wrappers);
        roleForm.setEnabled(role.getEnabled());
        model.addAttribute("roleForm", roleForm);
        return "role";
    }

    /**
     * Cancel editing role
     *
     * @return redirect to roles.html
     */
    @PostMapping(value = "/addRole", params = "action=cancel")
    public String cancelAddRole() {
        return "redirect:/admin/roles";
    }

    /**
     * Removes role with given ID from database
     *
     * @param id - id of role to remove
     * @return redirect to roles.html
     */
    @Transactional
    @PostMapping(value = "/role/remove", params = "action=remove")
    public String removeRole(Model model, @ModelAttribute("id") Long id, @ModelAttribute("force") Boolean force) {
        log.info("Remove role by id {} with force: {}", id, force);
        var role = roleRepository.findById(id);
        if (role.isEmpty()) {
            return "redirect:/admin/roles";
        }
        List<User> users = userRepository.findAllBySelectedRoleId(role.get().getId());


        if (!force) {
            if (users.isEmpty()) {
                String confirmMsgTemplate = "Подтвердите удаление роли " + role.get().getName();
                model.addAttribute("errorMessage", confirmMsgTemplate);
            } else {
                List<String> usernames = users.stream().map(User::getUsername).collect(Collectors.toList());
                String confirmMsgTemplate = "Роль " + role.get().getName() + " используется следующими пользователями: " + usernames + ". Действительно удалить?";
                model.addAttribute("errorMessage", confirmMsgTemplate);
            }

            var roles = roleRepository.findAll();
            model.addAttribute("roles", roles);
            return "roles";
        }

        if (users.isEmpty()) {
            roleRepository.deleteById(role.get().getId());
            log.info("Role with id {} removed with force: {}", id, force);
            return "redirect:/admin/roles";
        }

        for (User user : users) {
            user.getRoles().remove(role.get());
            log.info("Remove role with id {} from user with username {} and id {}", role.get().getId(), user.getUsername(), user.getId());
            userRepository.save(user);
        }

        roleRepository.deleteById(role.get().getId());
        log.info("Remove role by id {}", id);
        return "redirect:/admin/roles";
    }

    /**
     * @param model - thymeleaf view model
     * @return users.html with users list
     */
    @GetMapping(value = "/users")
    public String usersList(Model model) {
        var users = userRepository.findAllWithRoles();
        model.addAttribute("users", users);
        return "users";
    }

    /**
     * @param model - thymeleaf view model
     * @return addUser.html with form for user creating
     */
    @GetMapping(value = "/addUser")
    public String showAddUserForm(Model model) {
        List<RoleWrapper> roleWrappers = roleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Role::getName))
                .map(r -> new RoleWrapper(r, false))
                .collect(Collectors.toList());
        UserForm userForm = new UserForm();
        userForm.setEnabled(true);
        userForm.setRolesWrappers(roleWrappers);
        model.addAttribute("userForm", userForm);
        return "addUser";
    }

    /**
     * @return redirect to users.html
     */
    @PostMapping(value = "/addUser", params = "action=cancel")
    public String cancelAddUser() {
        return "redirect:/admin/users";
    }

    /**
     * @param model - thymeleaf view model
     * @param id    - id of user for editing
     * @return addUser.html with pre-loaded user for editing
     */
    @GetMapping("/user/edit/{id}")
    public String goToEditUser(Model model, @PathVariable(value = "id") Long id) {
        UserForm userForm = new UserForm();
        var userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            log.error("Try to edit non-existing user with id {}", id);
            return "redirect:/admin/users";
        }
        User user = userOptional.get();
        userForm.setEnabled(user.getEnabled());
        userForm.setId(user.getId());
        userForm.setUsername(user.getUsername());
        List<RoleWrapper> selectedWrappers = userRepository.findSelectedRoleWrappersByUserId(user.getId());
        List<RoleWrapper> notSelectedWrappers = roleRepository.findNotSelectedRoleWrappersByUserId(user.getId());
        List<RoleWrapper> wrappers = new ArrayList<>(selectedWrappers);
        wrappers.addAll(notSelectedWrappers);
        wrappers.sort(Comparator.comparing(RoleWrapper::getName));
        userForm.setRolesWrappers(wrappers);
        model.addAttribute("userForm", userForm);
        log.info("Go to editing user with username {} and id {}", user.getUsername(), user.getId());
        return "addUser";
    }

    /**
     * @param model - thymeleaf view model
     * @param id    - id of user to remove
     * @return redirect to users.html
     */
    @PostMapping(value = "/user/remove", params = "action=remove")
    public String removeUser(Model model,
                             @ModelAttribute("id") Long id, @ModelAttribute("force") Boolean force) {
        log.info("Remove user by id {}", id);
        var user = userRepository.findById(id);
        if (user.isEmpty()) {
            return "redirect:/admin/users";
        }

        if (!force) {
            String confirmMsgTemplate = "Подтвердите удаление пользователя " + user.get().getUsername();
            model.addAttribute("errorMessage", confirmMsgTemplate);
            var users = userRepository.findAllWithRoles();
            model.addAttribute("users", users);
            return "users";
        }

        userRepository.deleteById(id);
        log.info("User removed by id {}", id);
        return "redirect:/admin/users";
    }

    /**
     * Creates user and associate it with selected roles
     *
     * @param model    - thymeleaf view model
     * @param userForm - User data for user creating
     * @return redirect to users.html or error message
     */
    @PostMapping(value = "/addUser", params = "action=save")
    public String saveUser(Model model, @ModelAttribute("userForm") UserForm userForm) {
        if (userForm.getUsername() == null || userForm.getUsername().isBlank()) {
            log.error("Creating user failed {}. Username is empty", userForm);
            model.addAttribute("errorMessage", "Имя не может быть пустым");
            return "addUser";
        }
        User user = null;
        User userByName = userRepository.findByUsername(userForm.getUsername());
        if (userForm.getId() == null) {
            if (userByName != null) {
                log.error("Creating user failed {}. Username is busy", userForm);
                model.addAttribute("errorMessage", "Пользователь с таким именем уже существует");
                return "addUser";
            }
            log.info("Create new user {}", userForm);
            user = new User();
        } else {
            Optional<User> userOpt = userRepository.findById(userForm.getId());
            if (userOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Редактируемый пользователь не найден.");
                return "addUser";
            }
            user = userOpt.get();
            if (userByName != null && !userByName.getId().equals(user.getId())) {
                log.error("Creating user failed {}. Username is busy", userForm);
                model.addAttribute("errorMessage", "Пользователь с таким именем уже существует");
                return "addUser";
            }
        }

        user.setEnabled(userForm.getEnabled());
        user.setUsername(userForm.getUsername());
        if (userForm.getRolesWrappers() != null && userForm.getRolesWrappers().size() > 0) {
            Set<Role> selectedRoutes = new HashSet<>(roleRepository.findAllById(userForm.getRolesWrappers()
                    .stream()
                    .filter(RoleWrapper::getSelected)
                    .map(RoleWrapper::getId)
                    .collect(Collectors.toList())));
            user.setRoles(selectedRoutes);
        }

        userRepository.save(user);
        log.info("Saved user {}", user);
        return "redirect:/admin/users";
    }

    /**
     * @param model - thymeleaf view model
     * @return editSettings.html with pre-loaded setting to edit
     */
    @GetMapping("/settings/edit")
    public String goToEditSettings(Model model) {
        SettingsForm settingsForm = new SettingsForm();
        Preferences preferences = preferencesRepository.findAll().get(0);
        settingsForm.setAuthEnabled(preferences.getAuthEnabled());
        settingsForm.setAuthHeader(preferences.getAuthHeader());
        model.addAttribute("settingsForm", settingsForm);
        return "editSettings";
    }

    /**
     * @param model        - thymeleaf view model
     * @param settingsForm - settings data
     * @return redirect to routes.html or error message
     */
    @PostMapping(value = "/settings/edit", params = "action=save")
    public String saveSettings(Model model, @ModelAttribute("settingsForm") SettingsForm settingsForm) {
        Preferences preferences = preferencesRepository.findAll().get(0);
        if (settingsForm.getAuthHeader() == null || settingsForm.getAuthHeader().isBlank()) {
            model.addAttribute("errorMessage", "Имя заголовока не может быть пустым");
            return "editSettings";
        }
        preferences.setAuthEnabled(settingsForm.getAuthEnabled());
        preferences.setAuthHeader(settingsForm.getAuthHeader());
        preferencesRepository.save(preferences);
        return "redirect:/admin/routes";
    }

    /**
     * @return redirect to routes.html
     */
    @PostMapping(value = "/settings/edit", params = "action=cancel")
    public String cancelEditSettings() {
        return "redirect:/admin/routes";
    }

}
