package com.tuneit.itc.admin;

import lombok.Data;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.RoleChecker;

@Data
@ViewScoped
@ManagedBean
public class AdministrativeBean {
    private Log log = LogFactoryUtil.getLog(this.getClass());
    @ManagedProperty("#{liferay}")
    private Liferay liferay;

    public boolean canViewContractorList() {
        long userId = LiferayPortletHelperUtil.getUserId();
        boolean isOmni = liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
        return RoleChecker.hasAnyGlobalRole(userId, RoleConstants.CONTRACTORS_MANAGER) || isOmni;
    }

    public boolean canViewCreateContractorPage() {
        long userId = LiferayPortletHelperUtil.getUserId();
        boolean isOmni = liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
        return RoleChecker.hasAnyGlobalRole(userId, RoleConstants.CONTRACTORS_MANAGER) || isOmni;
    }

    public boolean canViewGivenContractorPage(Long groupId) {
        long userId = LiferayPortletHelperUtil.getUserId();
        boolean isOmni = liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
        boolean isManager = RoleChecker.hasAnyGlobalRole(userId, RoleConstants.CONTRACTORS_MANAGER);
        boolean isContractorAdmin = RoleChecker.hasAnyGroupRole(userId, groupId, RoleConstants.CONTRACTOR_ADMIN);
        return isManager || isContractorAdmin || isOmni;
    }

    public boolean canViewContractorPage() {
        boolean isOmni = liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
        long userId = LiferayPortletHelperUtil.getUserId();
        boolean contractorManager = RoleChecker.hasAnyGlobalRole(userId, RoleConstants.CONTRACTORS_MANAGER);
        boolean contractorAdmin = RoleChecker.hasAnyGroupRole(userId, RoleConstants.CONTRACTOR_ADMIN);
        return contractorManager || contractorAdmin || isOmni;
    }

    public boolean canViewClaimsListPage() {
        boolean isOmni = liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
        long userId = LiferayPortletHelperUtil.getUserId();
        return RoleChecker.hasAnyGlobalRole(userId, RoleConstants.CONTRACTORS_MANAGER) || isOmni;
    }
}
