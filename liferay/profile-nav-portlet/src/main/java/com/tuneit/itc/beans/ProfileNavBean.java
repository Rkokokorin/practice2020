package com.tuneit.itc.beans;

import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.util.StringUtil;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.RoleChecker;

@Data
@ManagedBean
@ViewScoped
public class ProfileNavBean {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Long userId;
    private boolean isAdmin;

    private String profileSvgIcon;
    private String ordersSvgIcon;
    private String favoriteSvgIcon;
    private String adminPanelSvgIcon;

    @PostConstruct
    public void init() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            final var profile = ec.getResourceAsStream("/WEB-INF/resources/images/nav-profile-icon.svg");
            final var orders = ec.getResourceAsStream("/WEB-INF/resources/images/nav-orders-icon.svg");
            final var favorite = ec.getResourceAsStream("/WEB-INF/resources/images/nav-favorite-icon.svg");
            final var adminPanel = ec.getResourceAsStream("/WEB-INF/resources/images/nav-orders-icon.svg");
            profileSvgIcon = StringUtil.read(profile);
            ordersSvgIcon = StringUtil.read(orders);
            favoriteSvgIcon = StringUtil.read(favorite);
            adminPanelSvgIcon = StringUtil.read(adminPanel);
        } catch (Exception exc) {
            log.error("Error while svg initialization!", exc);
        }

        this.userId = LiferayPortletHelperUtil.getUserId();
        this.isAdmin = RoleChecker.hasAnyGroupRole(userId, RoleConstants.CONTRACTOR_ADMIN)
            || RoleChecker.hasAnyGlobalRole(userId, RoleConstants.CONTRACTOR_ADMIN_GLOBAL)
            || LiferayPortletHelperUtil.getThemeDisplay().getPermissionChecker().isOmniadmin()
        ;
    }

}
