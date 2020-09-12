package com.tuneit.itc.admin.contractors;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PwdGenerator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import com.tuneit.itc.commons.Pair;
import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.RoleChecker;
import com.tuneit.itc.commons.model.ExtendedOrganization;

@ManagedBean
@ViewScoped
@Data
public class ContractorEmployeeCreationBean implements Serializable {
    private String name;
    private String surname;
    private String patronymic;
    private String email;
    private Long contractorId;
    private Long editedUserId;
    private boolean isMale;
    private boolean admin;
    private boolean disabled;
    private boolean editMode;

    private Log log = LogFactoryUtil.getLog(this.getClass());

    @ManagedProperty("#{contractorViewBean}")
    private ContractorViewBean contractorViewBean;

    @PostConstruct
    public void init() {
        contractorId = null;
        clear();
    }

    private void clear() {
        name = "";
        surname = "";
        patronymic = "";
        email = "";
        isMale = true;
        admin = false;
        disabled = false;
        editedUserId = null;
        editMode = false;
    }

    public void goToCreation(Long contractorId) {
        clear();
        this.contractorId = contractorId;
    }

    public void goToEditing(Long contractorId, Long userId) {
        clear();
        this.contractorId = contractorId;
        this.editedUserId = userId;
        User u = null;
        try {
            u = UserLocalServiceUtil.getUser(userId);
        } catch (PortalException ex) {
            log.error("Cannot find user", ex);
            return;
        }
        name = u.getFirstName();
        surname = u.getLastName();
        patronymic = u.getMiddleName();
        email = u.getEmailAddress();
        disabled = !u.isActive();
        admin = RoleChecker.hasAnyGroupRole(u.getUserId(), RoleConstants.CONTRACTOR_ADMIN);
        editMode = true;
    }

    public void create() {
        ThemeDisplay td = LiferayPortletHelperUtil.getThemeDisplay();
        long companyId = td.getCompanyId();
        log.info("User " + td.getUserId() + " creates new employee");

        if (editedUserId == null && checkUserExisting(companyId)) {
            return;
        }
        Organization organization = null;
        try {
            organization = OrganizationLocalServiceUtil.getOrganization(contractorId);
        } catch (PortalException e) {
            log.warn("Can not fetch contractor", e);
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            return;
        }
        ExtendedOrganization extendedOrganization = new ExtendedOrganization(organization);
        if (!extendedOrganization.isJuridicalPerson()) {
            log.warn("Can not create employee for private person contractor");
            LocalizedFacesMessage.error("itc.admin.contractor.employee.private-person.error");
            return;
        }

        List<Role> groupRoles = new ArrayList<>();
        List<Role> globalRoles = new ArrayList<>();
        try {
            groupRoles.add(RoleLocalServiceUtil.getRole(td.getCompanyId(), RoleConstants.CONTRACTOR_EMPLOYEE));
        } catch (PortalException e) {
            log.warn("Can not fetch role", e);
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            return;
        }

        if (admin) {
            try {
                groupRoles.add(RoleLocalServiceUtil.getRole(td.getCompanyId(), RoleConstants.CONTRACTOR_ADMIN));
                globalRoles.add(RoleLocalServiceUtil.getRole(td.getCompanyId(), RoleConstants.CONTRACTOR_ADMIN_GLOBAL));
            } catch (PortalException e) {
                log.warn("Can not fetch role", e);
                LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
                return;
            }
        }


        try {
            if (editedUserId == null) {
                createUser(td, companyId, organization, groupRoles, globalRoles);
                clear();
                LocalizedFacesMessage.info("itc.admin.contractor.employee.created");

            } else {
                if (updateUser(td, editedUserId, companyId, organization, groupRoles, globalRoles)) {
                    LocalizedFacesMessage.info("itc.admin.contractor.employee.updated");
                }
            }
        } catch (UserEmailAddressException invalidAddress) {
            LocalizedFacesMessage.error("itc.admin.invalid-email");
            return;
        } catch (PortalException exception) {
            log.warn("Error!", exception);
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            return;
        }
    }

    private User findUserByEmail(long companyId, String email) throws PortalException {
        try {
            return UserLocalServiceUtil.getUserByEmailAddress(companyId, email);
        } catch (NoSuchUserException ignored) {
            return null;
        }
    }

    private boolean checkUserExisting(long companyId) {
        User user = null;
        try {
            user = findUserByEmail(companyId, email);
        } catch (PortalException e) {
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            log.warn("Error!", e);
            return true;
        }
        if (user != null) {
            log.warn("User with email " + email + " exists!");
            LocalizedFacesMessage.error("itc.admin.contractor.user-exists");
        }
        return user != null;
    }

    private boolean updateUser(ThemeDisplay td, long userId, long companyId,
                               Organization organization, List<Role> groupRoles,
                               List<Role> globalRoles) {
        var userResult = actionWithErrorChecking(() -> UserLocalServiceUtil.getUser(userId),
                "User [" + userId + "] not found!", "itc.admin.user-not-found");
        if (!userResult.getFirst()) {
            return false;
        }
        User oldUser = userResult.getSecond();
        if (!Objects.equals(email, oldUser.getEmailAddress())) {
            if (checkUserExisting(companyId)) {
                return false;
            }
        }

        oldUser.setEmailAddress(email);
        oldUser.setFirstName(name);
        oldUser.setMiddleName(patronymic);
        oldUser.setLastName(surname);
        oldUser.setStatus(disabled
                ? WorkflowConstants.STATUS_INACTIVE
                : WorkflowConstants.STATUS_APPROVED
        );
        try {
            UserLocalServiceUtil.updateUser(oldUser);
        } catch (Exception exc) {
            log.warn("Error while update user! " + exc.getMessage());
            log.debug(exc);
            return false;
        }
        log.info("User was updated");
        UserGroupRoleLocalServiceUtil.deleteUserGroupRolesByUserId(
                oldUser.getUserId());
        for (Role role : groupRoles) {
            UserGroupRoleLocalServiceUtil.addUserGroupRoles(oldUser.getUserId(), organization.getGroupId(),
                    new long[]{role.getRoleId()});
            log.info("Granting group role <" + role.getName() + "> to user <" + email + ">");
        }

        var roleResult = actionWithErrorChecking(
            () -> RoleLocalServiceUtil.getRole(td.getCompanyId(), RoleConstants.CONTRACTOR_ADMIN_GLOBAL),
                "Can not find contractor admin role:", "itc.admin.contractor-global-role-not-found");
        if (!roleResult.getFirst()) {
            return false;
        }
        Role contractorAdminGlobalRole = roleResult.getSecond();
        var deleteResult = actionWithErrorChecking(() -> {
            UserLocalServiceUtil.deleteRoleUser(
                    contractorAdminGlobalRole
                            .getRoleId(),
                    oldUser.getUserId());
            return null;
        }, "Can not delete role:", "itc.admin.can-not-delete-role");
        if (!deleteResult.getFirst()) {
            return false;
        }
        for (Role role : globalRoles) {
            UserLocalServiceUtil.addRoleUser(role.getRoleId(), oldUser.getUserId());
            log.info("Granting role <" + role.getName() + "> to user <" + email + ">");
        }
        log.info("Role was added");
        return true;
    }


    private void createUser(ThemeDisplay td, long companyId, Organization organization, List<Role> groupRoles,
                            List<Role> globalRoles) throws PortalException {
        ServiceContext serviceContext = new ServiceContext();
        User newUser = UserLocalServiceUtil.addUser(td.getUserId(), companyId, true, null, null,
                true, null, email, 0, null, td.getSiteDefaultLocale(),
                name, patronymic, surname, 0, 0, isMale, 1, 1,
                1970, null, null, new long[]{organization.getOrganizationId()}, null, null,
                true, serviceContext);
        newUser.setStatus(disabled
                ? WorkflowConstants.STATUS_INACTIVE
                : WorkflowConstants.STATUS_APPROVED
        );
        UserLocalServiceUtil.updateUser(newUser);
        log.info("User was created");
        for (Role role : groupRoles) {
            UserGroupRoleLocalServiceUtil.addUserGroupRoles(newUser.getUserId(), organization.getGroupId(),
                    new long[]{role.getRoleId()});
        }

        for (Role role : globalRoles) {
            UserLocalServiceUtil.addRoleUser(role.getRoleId(), newUser.getUserId());
        }
        log.info("Role was added");
    }

    public void goBack() {
        clear();
        contractorId = null;
        contractorViewBean.updateUsers();
    }

    public void resetPassword(long userId) {
        User oldUser = null;
        try {
            oldUser = UserLocalServiceUtil.getUser(userId);
        } catch (PortalException ex) {
            log.error("User [" + userId + "] not found!", ex);
            return;
        }
        if (null == oldUser) {
            log.error("User [" + userId + "] not found!");
            return;
        }
        oldUser.setPasswordReset(true);
        try {
            String password = PwdGenerator.getPassword(25);
            UserLocalServiceUtil.updatePassword(userId, password, password, false, true);
            ServiceContext sc = (ServiceContext) ServiceContextThreadLocal.getServiceContext().clone();
            long groupId = LiferayPortletHelperUtil.getThemeDisplay().getCompanyGroupId();
            long defaultPlid = LayoutLocalServiceUtil.getDefaultPlid(groupId);
            sc.setPlid(defaultPlid);
            UserLocalServiceUtil.sendPassword(oldUser.getCompanyId(), oldUser.getEmailAddress(),
                    null, null, null, null, sc);
            LocalizedFacesMessage.infoFormat("itc.admin.contractor.password-was-reset",
                    new Object[]{oldUser.getFullName(), oldUser.getEmailAddress()});
        } catch (PortalException exc) {
            log.warn("Unexpected error! Can not reset password.", exc);
            LocalizedFacesMessage.errorFormat("itc.admin.contractor.password-reset-error",
                    new Object[]{oldUser.getFullName(), oldUser.getEmailAddress()});
        }
    }

    private <T> Pair<Boolean, T> actionWithErrorChecking(PortalAction<T> action, String logErrorMessage,
                                                         String facesMessageKey) {
        try {
            T result = action.doAction();
            return Pair.of(true, result);
        } catch (PortalException exc) {
            log.warn(logErrorMessage + " " + exc.getMessage());
            log.debug(exc);
            LocalizedFacesMessage.error(facesMessageKey);
            return Pair.of(false, null);
        }
    }

    private interface PortalAction<T> {
        T doAction() throws PortalException;
    }
}