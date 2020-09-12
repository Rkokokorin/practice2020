package com.tuneit.itc.admin.contractors;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.model.ContractorSyntheticId;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.OrganizationRegistrationClaim;
import com.tuneit.itc.commons.service.ContractorSyntheticIdService;
import com.tuneit.itc.commons.service.OrganizationRegistrationService;

import static com.tuneit.itc.commons.Pair.of;

@ManagedBean
@ViewScoped
@Data
public class ContractorRegistrationBean implements Serializable {
    private static final Log log = LogFactoryUtil.getLog(ContractorRegistrationBean.class);
    public static final String CLAIM_ID_PARAM = "claimId";
    private String msrn;
    private String tin;
    private String name;
    private String shortName;
    private String adminName;
    private String adminSurname;
    private String adminPatronymic;
    private String adminEmail;
    private String adminUsername;
    private String backOfficeCode;
    private boolean provider;
    private boolean buyer;
    private boolean legalEntity;
    private boolean isMale;
    private String phone;


    private OrganizationRegistrationClaim openedClaim;

    private UIComponent msrnInput;
    private UIComponent tinInput;
    private UIComponent fullNameInput;
    private UIComponent shortNameInput;
    private UIComponent backOfficeCodeInput;

    @ManagedProperty("#{organizationRegistrationService}")
    private OrganizationRegistrationService service;
    @ManagedProperty("#{contractorSyntheticIdService}")
    private ContractorSyntheticIdService idService;


    @PostConstruct
    public void init() {
        clear();
        String claimIdParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get(CLAIM_ID_PARAM);
        log.info("Claim id: " + claimIdParam);
        if (claimIdParam != null) {
            long claimId = Long.parseLong(claimIdParam);
            OrganizationRegistrationClaim claim = service.find(claimId).orElse(null);
            if (claim != null) {
                if (Objects.equals(claim.getAssignedUser(), LiferayPortletHelperUtil.getUserId())) {
                    openedClaim = claim;
                    fillFromClaim(claim);
                }
            }
        }
    }

    private void fillFromClaim(OrganizationRegistrationClaim claim) {
        this.msrn = claim.getMsrn();
        this.tin = claim.getTin();
        this.name = claim.getCompanyFullName();
        this.shortName = claim.getCompanyShortName();
        this.adminName = claim.getAdminName();
        this.adminSurname = claim.getAdminSurname();
        this.adminPatronymic = claim.getAdminPatronymicName();
        this.adminEmail = claim.getEmail();
        this.legalEntity = claim.isJuridicalPerson();
        this.isMale = true;
        this.phone = claim.getPhone();
    }

    private void clear() {
        msrn = "";
        tin = "";
        name = "";
        shortName = "";
        adminName = "";
        adminSurname = "";
        adminPatronymic = "";
        adminEmail = "";
        adminUsername = "";
        backOfficeCode = "";
        provider = false;
        buyer = true;
        legalEntity = true;
        isMale = true;
        openedClaim = null;
        phone = "";
    }

    public void create() {
        ThemeDisplay td = LiferayPortletHelperUtil.getThemeDisplay();
        long companyId = td.getCompanyId();
        log.info("User " + td.getUserId() + " creates new contractor");
        if (!validateFields()) {
            log.warn("Invalid juridical person fields");
            return;
        }
        if (checkUserExisting(companyId)) {
            return;
        }
        Organization organization;
        try {
            organization = createOrganization(td);
            log.info("Created organization with id " + organization.getOrganizationId());
        } catch (PortalException e) {
            log.warn("Can not create organization!", e);
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            return;
        }
        List<Role> groupRoles = new ArrayList<>();
        List<Role> globalRoles = new ArrayList<>();
        try {
            groupRoles.add(RoleLocalServiceUtil.getRole(td.getCompanyId(), RoleConstants.CONTRACTOR_ADMIN));
            groupRoles.add(RoleLocalServiceUtil.getRole(td.getCompanyId(), RoleConstants.CONTRACTOR_EMPLOYEE));
        } catch (PortalException e) {
            log.warn("Can not fetch role", e);
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            return;
        }

        if (isLegalEntity()) {
            try {
                globalRoles.add(RoleLocalServiceUtil.getRole(td.getCompanyId(), RoleConstants.CONTRACTOR_ADMIN_GLOBAL));
            } catch (PortalException e) {
                log.warn("Can not fetch role", e);
                LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
                return;
            }
        }
        try {
            createUser(td, companyId, organization, groupRoles, globalRoles);
        } catch (UserEmailAddressException invalidAddress) {
            LocalizedFacesMessage.error("itc.admin.invalid-email");
            return;
        } catch (PortalException exception) {
            log.warn("Error!", exception);
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            return;
        }
        if (openedClaim != null) {
            openedClaim.setActive(false);
            openedClaim.setAssociatedOrganization(organization.getOrganizationId());
            service.save(openedClaim);
            LocalizedFacesMessage.info("itc.admin.claim.closed");
        }
        clear();
        LocalizedFacesMessage.info("itc.admin.contractor.created");
    }

    public boolean validateFields() {
        if (!isLegalEntity()) {
            log.info("Non-juridical fields validation results: " + (
                            Stream.of(of(backOfficeCodeInput, backOfficeCode))
                                    .filter(kv -> empty(kv.getSecond()))
                                    .peek(kv -> reportRequiredField(kv.getFirst()))
                                    .count() == 0
                    )
            );
            return Stream.of(of(backOfficeCodeInput, backOfficeCode))
                    .filter(kv -> empty(kv.getSecond()))
                    .peek(kv -> reportRequiredField(kv.getFirst()))
                    .count() == 0;
        }
        log.info("Juridical fields validation results: " + (
                        Stream.of(of(msrnInput, msrn), of(tinInput, tin),
                                of(fullNameInput, name), of(shortNameInput, shortName),
                                of(backOfficeCodeInput, backOfficeCode))
                                .filter(kv -> empty(kv.getSecond()))
                                .peek(kv -> reportRequiredField(kv.getFirst()))
                                .count() == 0
                )
        );
        return Stream.of(of(msrnInput, msrn), of(tinInput, tin), of(fullNameInput, name),
                    of(shortNameInput, shortName), of(backOfficeCodeInput, backOfficeCode))
                .filter(kv -> empty(kv.getSecond()))
                .peek(kv -> reportRequiredField(kv.getFirst()))
                .count() == 0;
    }

    private void reportRequiredField(UIComponent component) {
        LocalizedFacesMessage.error(component.getClientId(), "itc.admin.contractor.required-field", null);
    }

    private boolean empty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean checkUserExisting(long companyId) {
        User user = null;
        try {
            user = UserLocalServiceUtil.getUserByEmailAddress(companyId, adminEmail);
        } catch (NoSuchUserException ignored) {
        } catch (PortalException e) {
            LocalizedFacesMessage.error("itc.admin.contractor.unknown-error");
            log.warn("Error!", e);
            return true;
        }
        if (user != null) {
            log.warn("User with email " + adminEmail + " exists!");
            LocalizedFacesMessage.error("itc.admin.contractor.user-exists");
        }
        return user != null;
    }

    private Organization createOrganization(ThemeDisplay td) throws PortalException {
        final Long id = idService.save(new ContractorSyntheticId()).getId();
        if (!isLegalEntity()) {
            fillOrgInfoFromUser();
        }
        Organization organization = OrganizationLocalServiceUtil
                .addOrganization(td.getUserId(), 0, name, false);
        log.info("Organization with id " + organization.getOrganizationId() + " was created");
        ExtendedOrganization extendedOrganization;
        extendedOrganization = new ExtendedOrganization(organization);
        extendedOrganization.setJuridicalPerson(legalEntity);
        extendedOrganization.setMsrn(msrn);
        extendedOrganization.setTin(tin);
        extendedOrganization.setShortName(shortName);
        extendedOrganization.setBackOfficeCode(backOfficeCode);
        extendedOrganization.setProvider(provider);
        extendedOrganization.setBuyer(buyer);
        extendedOrganization.setSyntheticId(id);
        extendedOrganization.setPhone(phone);
        OrganizationLocalServiceUtil.updateOrganization(extendedOrganization.getOrganization());
        log.info("Additional fields were updated in organization " + organization.getOrganizationId());
        return organization;
    }

    private void createUser(ThemeDisplay td, long companyId, Organization organization, List<Role> groupRoles,
                            List<Role> globalRoles) throws PortalException {
        ServiceContext serviceContext = new ServiceContext();
        User newUser = UserLocalServiceUtil.addUser(td.getUserId(), companyId, true, null, null,
                true, null, adminEmail, 0, null, td.getSiteDefaultLocale(),
                adminName, adminPatronymic, adminSurname, 0, 0, isMale, 1, 1,
                1970, null, null, new long[]{organization.getOrganizationId()}, null, null,
                true, serviceContext);
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

    private void fillOrgInfoFromUser() {
        name = Stream.of(adminSurname, adminName, adminPatronymic)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.joining(" "));
        shortName = name;
    }
}
