package com.tuneit.itc.cart;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.RoleChecker;

@Data
@ManagedBean
@ViewScoped
public class NavigationBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(NavigationBean.class);
    private List<Page> pages;
    private Page activePage;

    public static Page findActivePage() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        log.debug("Current view: {0}", viewId);
        return Arrays.stream(Page.values())
            .filter(page -> page.getViewId().equals(viewId))
            .findAny()
            .orElse(null);
    }

    @PostConstruct
    public void init() {
        if (isContractorEmployee() || isSalesManager()) {
            activePage = findActivePage();
            pages = Arrays.stream(Page.values())
                .filter(Page::isRendered)
                .collect(Collectors.toList());
        }
    }

    public boolean isContractorEmployee() {
        return RoleChecker.hasAnyGroupRole(LiferayPortletHelperUtil.getUserId(),
            RoleConstants.CONTRACTOR_EMPLOYEE, RoleConstants.CONTRACTOR_ADMIN);
    }

    public boolean isSalesManager() {
        return RoleChecker.hasAnyGlobalRole(LiferayPortletHelperUtil.getUserId(),
            RoleConstants.SALES_MANAGER, RoleConstants.SALES_DEPARTMENT_HEAD);
    }

    public enum Page {
        CURRENT_CART(Views.VIEW, "itc.cart.go-to-current"),
        MY_CARTS(Views.SAVED, "itc.cart.go-to-all") {
            @Override
            public boolean isRendered() {
                return LiferayPortletHelperUtil.getThemeDisplay().isSignedIn();
            }
        },
        COLLEAGUES_CARTS(Views.COLLEAGUES, "itc.cart.colleagues-carts") {
            @Override
            public boolean isRendered() {
                return LiferayPortletHelperUtil.getThemeDisplay().isSignedIn();
            }
        },
        CART_VIEW(Views.CART_VIEW, "itc.cart.cart-view-title") {
            @Override
            public boolean isRendered() {
                return findActivePage() == this;
            }
        },
        CLONE_PAGE(Views.CLONE, "itc.cart.clone-page-title") {
            @Override
            public boolean isRendered() {
                return findActivePage() == CLONE_PAGE;
            }
        },
        DELETED_CARTS(Views.DELETED, "itc.cart.deleted-page-title") {
            @Override
            public boolean isRendered() {
                return findActivePage() == DELETED_CARTS;
            }
        },
        SEARCH(Views.ASSISTANCE, "itc.cart.assistance-page-title") {
            @Override
            public boolean isRendered() {
                return RoleChecker.hasAnyGlobalRole(LiferayPortletHelperUtil.getUserId(), RoleConstants.SALES_MANAGER);
            }
        };
        @Getter
        private final String viewId;
        @Getter
        private final String localizationKey;

        Page(String viewId, String localizationKey) {
            this.viewId = viewId;
            this.localizationKey = localizationKey;
        }

        public boolean isRendered() {
            return true;
        }
    }
}
