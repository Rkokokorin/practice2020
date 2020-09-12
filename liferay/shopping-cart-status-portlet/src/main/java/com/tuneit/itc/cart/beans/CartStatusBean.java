package com.tuneit.itc.cart.beans;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.PortalUtil;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.RoleChecker;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.cart.Cart;
import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.WarehouseService;
import com.tuneit.itc.commons.service.cart.CartPositionService;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.util.CartUtil;

/**
 * View of current user cart status (positions count, total sum...)
 *
 * @author Valeriy Kireev
 */

@Data
@ViewScoped
@ManagedBean
public class CartStatusBean implements Serializable {

    private String userCurrency;
    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{warehouseService}")
    private WarehouseService warehouseService;

    private User user;
    private String cartId;

    private Cart currentCart;
    private List<CartPosition> order;

    private boolean viewAccess;

    @ManagedProperty("#{liferay}")
    private Liferay liferay;
    @ManagedProperty("#{cartService}")
    private CartService cartService;
    @ManagedProperty("#{cartPositionService}")
    private CartPositionService cartPositionService;

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Boolean cartUpdateStatus = false;
    private Warehouse selectedWarehouse;
    private Currency preferredCurrency;

    public void init() {
        if (log.isDebugEnabled()) {
            PortletRequest pr = (PortletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            HttpServletRequest sr = PortalUtil.getHttpServletRequest(pr);
            String addr = sr.getRemoteAddr();
            User user = LiferayPortletHelperUtil.getThemeDisplay().getUser();
            long userId = user.getUserId();
            String forwardedFor = sr.getHeader("X-Forwarded-For");
            String realIp = sr.getHeader(" X-Real-IP");
            log.debug("User [{0}, {1}, {2}] from address {3}, forwarded {4}, real ip {5}", userId,
                user.getScreenName(), user.getEmailAddress(), addr, forwardedFor, realIp);


        }
        this.user = liferay.getThemeDisplay().getUser();
        this.selectedWarehouse = warehouseService.getUserWarehouse();
        this.preferredCurrency = currencyService.getUserCurrency();
        this.userCurrency = preferredCurrency.getSign();
        currentCart = cartService.findCurrentForUser(this.user.getUserId());
        if (currentCart != null) {
            cartId = currentCart.getContractorCode() + "-" + currentCart.getContractorLocalCartId();
        }
        viewAccess = hasViewAccess();
        loadOrders();
    }

    public void updateCurrency() {
        this.userCurrency = currencyService.getUserCurrency().getSign();
        this.preferredCurrency = currencyService.getUserCurrency();
        this.selectedWarehouse = warehouseService.getUserWarehouse();
    }

    private void loadOrders() {
        if (!viewAccess) {
            return;
        }
        if (currentCart == null) {
            order = new ArrayList<>();
        } else {
            order = cartPositionService.findForCart(currentCart);
        }
    }

    public void reloadCart() {
        cartUpdateStatus = false;
        var currentCart = cartService.findCurrentForUser(user.getUserId());
        var order = cartPositionService.findForCart(currentCart);
        if (null == currentCart) {
            if (null != this.currentCart) {
                cartUpdateStatus = true;
            }
            this.currentCart = currentCart;
            viewAccess = hasViewAccess();
            loadOrders();
            return;
        }

        if (this.order == null && order != null) {
            cartUpdateStatus = true;
        } else if (this.order != null && order == null) {
            cartUpdateStatus = true;
        } else if (this.currentCart == null) {
            cartUpdateStatus = true;
        } else if (!currentCart.getId().equals(this.currentCart.getId())) {
            cartUpdateStatus = true;
        } else if (!Objects.equals(this.order, order)) {
            cartUpdateStatus = true;
        }

        if (cartUpdateStatus) {
            this.currentCart = currentCart;
            this.cartId = currentCart.getContractorCode() + "-" + currentCart.getContractorLocalCartId();
            this.viewAccess = hasViewAccess();
            loadOrders();
        }
    }

    public String getTotalPrice() {
        double total = CartUtil.computeTotal(this.order, preferredCurrency, selectedWarehouse);
        return String.format("%,.2f", total);
    }

    public Integer getTotalOrdersCount() {
        if (null == order || order.isEmpty()) {
            return 0;
        }
        return order.size();
    }

    private boolean hasViewAccess() {
        //TODO: additional logic (users of same contractor, admins, etc.)
        return currentCart == null || currentCart.getOwnerId() == user.getUserId() || isSalesManager();
    }


    public boolean isSalesManager() {
        return RoleChecker.hasAnyGlobalRole(LiferayPortletHelperUtil.getUserId(),
            RoleConstants.SALES_MANAGER, RoleConstants.SALES_DEPARTMENT_HEAD);
    }

}
