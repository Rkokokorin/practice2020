package com.tuneit.itc.contacts.claims;

import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import com.tuneit.itc.contacts.NavBean;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.model.OrganizationRegistrationClaim;
import com.tuneit.itc.commons.service.OrganizationRegistrationService;

@Data
@ManagedBean
@ViewScoped
public class ClaimsViewBean {
    private static final Log log = LogFactoryUtil.getLog(ClaimsViewBean.class);
    @ManagedProperty("#{organizationRegistrationService}")
    private OrganizationRegistrationService service;
    private List<OrganizationRegistrationClaim> claims;
    private ViewMode viewMode;
    private AssigneeFilter assigneeFilter;

    private Map<Long, User> assignedUsers;

    private OrganizationRegistrationClaim selectedClaim;
    private User assignedUser;

    @PostConstruct
    public void init() {
        this.viewMode = ViewMode.ACTIVE;
        this.assigneeFilter = AssigneeFilter.MINE;
        loadClaims();
    }

    public String goToPartnerCreation() {
        if (selectedClaim == null) {
            throw new IllegalStateException("Claim is not selected!");
        }
        return NavBean.Page.PARTNER_CREATION.getViewName();
    }

    @SuppressWarnings("unchecked")
    private void loadClaims() {
        switch (viewMode) {
            case ALL:
                claims = service.findAll();
                break;
            case ACTIVE:
                claims = service.findActive(true);
                break;
            case CLOSED:
                claims = service.findActive(false);
                break;
            default:
                assert false : "Unreachable statement";
        }
        switch (assigneeFilter) {
            case MINE:
                claims = claims.stream()
                        .filter(claim -> Objects.equals(claim.getAssignedUser(), LiferayPortletHelperUtil.getUserId()))
                        .collect(Collectors.toList());
                break;
            case UNASSIGNED:
                claims = claims.stream()
                        .filter(claim -> claim.getAssignedUser() == null)
                        .collect(Collectors.toList());
                break;
            case ALL:
                break;
            default:
                assert false : "Unreachable statement";
        }
        long[] userIds = claims.stream()
                .map(OrganizationRegistrationClaim::getAssignedUser)
                .filter(Objects::nonNull)
                .distinct()
                .mapToLong(i -> i)
                .toArray();
        log.info("User ids: " + Arrays.toString(userIds));
        if (userIds.length > 0) {
            DynamicQuery query = UserLocalServiceUtil
                    .dynamicQuery()
                    .add(PropertyFactoryUtil
                            .forName("userId")
                            .in(userIds));
            assignedUsers = UserLocalServiceUtil.dynamicQuery(query)
                    .stream()
                    .map(o -> (User) o)
                    .collect(Collectors.toMap(User::getUserId, x -> x));
        } else {
            assignedUsers = new HashMap<>();
        }
    }

    public void modeChange() {
        if (viewMode == null) {
            viewMode = ViewMode.ACTIVE;
        }
        loadClaims();
    }

    public void assigneeFilterChange() {
        if (assigneeFilter == null) {
            assigneeFilter = AssigneeFilter.MINE;
        }
        loadClaims();
    }

    public void onSelectClaim() {
        this.assignedUser = null;
        if (this.selectedClaim.getAssignedUser() != null) {
            try {
                this.assignedUser = UserLocalServiceUtil.getUser(this.selectedClaim.getAssignedUser());
            } catch (PortalException e) {
                log.warn("Error!", e);
            }
        }
    }

    public void assignClaim() {
        long userId = LiferayPortletHelperUtil.getUserId();
        if (selectedClaim.getAssignedUser() != null) {
            throw new IllegalStateException("Claim already assigned! Claim id: " + selectedClaim.getId());
        }
        selectedClaim.setAssignedUser(userId);
        this.selectedClaim = service.save(selectedClaim);
        onSelectClaim();
        LocalizedFacesMessage.info("itc.admin.claim.assigned");
    }

    public User claimAssignedUser(OrganizationRegistrationClaim claim) {
        if (claim.getAssignedUser() == null) {
            return null;
        }
        User user = assignedUsers.get(claim.getAssignedUser());
        if (user == null) {
            log.warn("Unexpected problem: user was assigned to claim, but not found. Claim: " + claim);
        }
        return user;
    }

    public String requesterFullName(OrganizationRegistrationClaim claim) {
        StringBuilder fullName = new StringBuilder();
        append(fullName, claim.getAdminSurname());
        append(fullName, claim.getAdminName());
        append(fullName, claim.getAdminPatronymicName());
        return fullName.toString().trim();
    }

    private void append(StringBuilder builder, String str) {
        if (str != null) {
            str = str.trim();
            if (!str.isEmpty()) {
                builder.append(str).append(" ");
            }
        }
    }

    public enum ViewMode {
        ACTIVE,
        ALL,
        CLOSED;
    }

    public enum AssigneeFilter {
        MINE,
        UNASSIGNED,
        ALL
    }
}
