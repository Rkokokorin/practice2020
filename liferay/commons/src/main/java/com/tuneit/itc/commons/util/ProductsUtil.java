package com.tuneit.itc.commons.util;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.service.rest.Requester;

public class ProductsUtil {
    public static String getUserName() {
        ThemeDisplay td = LiferayPortletHelperUtil.getThemeDisplay();
        if (td.isSignedIn()) {
            try {
                return td.getUser().getLogin();
            } catch (PortalException e) {
                return "Анонимный пользователь";
            }
        } else {
            return "Анонимный пользователь";
        }
    }

    public static Requester.ProductsService.UserExt getUserExt() {
        String partnerId = null;

        ThemeDisplay td = LiferayPortletHelperUtil.getThemeDisplay();
        if (td.isSignedIn()) {
            ExtendedOrganization partner = LiferayUtil.findContractorForUser(td.getUserId());
            if (partner != null) {
                partnerId = partner.getShortName();
            }
        }

        return new Requester.ProductsService.UserExt(
                partnerId,
                HttpUtil.getOriginalServletRequest().getRemoteAddr(),
                null, //TODO get it in future
                HttpUtil.getPortletServletRequest().getRequestedSessionId(),
                null //TODO geo
        );
    }
}
