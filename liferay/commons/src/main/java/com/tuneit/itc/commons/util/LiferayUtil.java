package com.tuneit.itc.commons.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import com.tuneit.itc.commons.model.ExtendedOrganization;

public class LiferayUtil {

    private static final Log log = LogFactoryUtil.getLog(LiferayUtil.class);

    public static ExtendedOrganization findContractorForUser(long userId) {
        List<Organization> userOrganizations = OrganizationLocalServiceUtil.getUserOrganizations(userId);
        List<ExtendedOrganization> extendedOrganizations = userOrganizations.stream()
            .map(ExtendedOrganization::new)
            .filter(eo -> Objects.nonNull(eo.getBackOfficeCode()))
            .filter(eo -> !eo.getBackOfficeCode().isBlank())
            .collect(Collectors.toList());
        if (extendedOrganizations.isEmpty()) {
            return null;
        }
        ExtendedOrganization firstContractor = extendedOrganizations.get(0);
        if (extendedOrganizations.size() > 1) {
            log.warn("User has multiple contractors. Use first " + firstContractor);
        }
        return firstContractor;
    }

    public static LiferayPortletURL getPortletUrl(String portletId) throws PortalException {
        PortletRequest request = (PortletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        long scopeGroupId = LiferayPortletHelperUtil.getScopeGroupId();
        long plid = PortalUtil.getPlidFromPortletId(scopeGroupId, portletId);
        LiferayPortletURL red = PortletURLFactoryUtil.create(request, portletId, plid, PortletRequest.RENDER_PHASE);
        return red;
    }
}
