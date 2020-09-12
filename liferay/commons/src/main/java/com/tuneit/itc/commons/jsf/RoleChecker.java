package com.tuneit.itc.commons.jsf;

import java.util.Arrays;
import java.util.List;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.RoleModel;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;

import com.tuneit.itc.commons.RoleConstants;

public class RoleChecker {
    private static Log log = LogFactoryUtil.getLog(RoleChecker.class);

    public static boolean hasAnyGlobalRole(Long userId, List<String> roleNames) {
        return RoleLocalServiceUtil.getUserRoles(userId).stream().map(RoleModel::getName).anyMatch(roleNames::contains);
    }

    public static boolean hasAnyGlobalRole(Long userId, String... roleNames) {
        return hasAnyGlobalRole(userId, Arrays.asList(roleNames));
    }

    public static boolean hasAnyGroupRole(Long userId, Long groupId, List<String> roleNames) {
        return UserGroupRoleLocalServiceUtil
            .getUserGroupRoles(userId, groupId)
            .stream()
            .map(ugr -> {
                try {
                    return ugr.getRole();
                } catch (PortalException e) {
                    throw new RuntimeException("No such role");
                }
            })
            .map(RoleModel::getName)
            .anyMatch(roleNames::contains);
    }

    public static boolean hasAnyGroupRole(Long userId, Long groupId, String... roleNames) {
        return hasAnyGroupRole(userId, groupId, Arrays.asList(roleNames));
    }

    public static boolean hasAnyGroupRole(Long userId, List<String> roleNames) {

        return UserGroupRoleLocalServiceUtil.getUserGroupRoles(userId)
            .stream()
            .map(ugr -> {
                try {
                    return ugr.getRole();
                } catch (PortalException e) {
                    throw new RuntimeException("No such role");
                }
            })
            .map(RoleModel::getName)
            .anyMatch(roleNames::contains);
    }

    public static boolean hasAnyGroupRole(Long userId, String... roleNames) {
        return hasAnyGroupRole(userId, Arrays.asList(roleNames));
    }

    public static boolean canAddToCart(long userId) {
        return RoleChecker.hasAnyGroupRole(userId, RoleConstants.CONTRACTOR_EMPLOYEE, RoleConstants.CONTRACTOR_ADMIN)
            || RoleChecker.hasAnyGlobalRole(userId, RoleConstants.SALES_DEPARTMENT_HEAD, RoleConstants.SALES_MANAGER);
    }

    public static boolean canAddToCart() {
        return canAddToCart(LiferayPortletHelperUtil.getUserId());
    }
}
