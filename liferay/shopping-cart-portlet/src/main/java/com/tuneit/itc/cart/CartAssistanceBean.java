package com.tuneit.itc.cart;

import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.RoleChecker;
import com.tuneit.itc.commons.model.ExtendedUser;
import com.tuneit.itc.commons.model.cart.Cart;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.WarehouseService;
import com.tuneit.itc.commons.service.cart.CartService;

@Data
@ManagedBean
@ViewScoped
public class CartAssistanceBean implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(CartAssistanceBean.class);
    @ManagedProperty("#{cartService}")
    private CartService cartService;
    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{warehouseService}")
    private WarehouseService warehouseService;
    private String orderNumber;
    private Cart foundCart;
    private User foundCartOwner;
    private Cart currentCart;
    private User currentCartOwner;
    private boolean salesManager = false;

    @SneakyThrows
    @PostConstruct
    public void init() {
        long userId = LiferayPortletHelperUtil.getUserId();
        salesManager = RoleChecker.hasAnyGlobalRole(userId, RoleConstants.SALES_MANAGER);
        if (salesManager) {
            currentCart = cartService.findCurrentForUser(userId);
            if (currentCart != null) {
                currentCartOwner = UserLocalServiceUtil.getUser(currentCart.getOwnerId());
            }
        }
    }

    public void searchCart() {
        foundCart = null;
        foundCartOwner = null;
        String trimmedNumber = orderNumber.trim();
        int separatorIndex = trimmedNumber.lastIndexOf('-');
        if (separatorIndex < 0) {
            LocalizedFacesMessage.error("itc.cart.assistance.invalid-search-format");
            return;
        }
        String contractorCode = trimmedNumber.substring(0, separatorIndex);
        String cartCodeStr = trimmedNumber.substring(separatorIndex + 1);
        long cartCode;
        logger.debug("Requested number: {0}, contractor: {1}, cart {2}", orderNumber, contractorCode, cartCodeStr);
        try {
            cartCode = Long.parseLong(cartCodeStr);
        } catch (NumberFormatException exc) {
            logger.debug("Cart id is not a number");
            LocalizedFacesMessage.error("itc.cart.assistance.invalid-search-format");
            return;
        }

        foundCart = cartService.find(contractorCode, cartCode);
        if (foundCart == null) {
            LocalizedFacesMessage.info("itc.cart.assistance.not-found");
        } else {
            try {
                foundCartOwner = UserLocalServiceUtil.getUser(foundCart.getOwnerId());
            } catch (PortalException exc) {
                logger.error(exc);
                LocalizedFacesMessage.error("itc.cart.assistance.user-load-error");
            }
        }
    }

    public void assignFoundCart() {
        if (foundCart == null) {
            throw new IllegalStateException("Found cart is null - can not assign");
        }
        long userId = LiferayPortletHelperUtil.getUserId();
        Cart currentUserCart = cartService.findCurrentForUser(userId);
        if (currentUserCart != null) {
            LocalizedFacesMessage.error("itc.cart.assistance.current-exists");
            return;
        }
        cartService.assignAsCurrent(foundCart, userId);
        currentCart = foundCart;
        currentCartOwner = foundCartOwner;
        ExtendedUser user = new ExtendedUser(LiferayPortletHelperUtil.getUser());
        user.setCurrencyCode(currencyService.getUserCurrency(currentCartOwner).getCode());
        user.setWarehouseCode(warehouseService.getUserWarehouse(currentCartOwner).getCode());
        user.getUser().persist();
    }

    public void unassignCurrent() {
        cartService.unassignCart(currentCart, LiferayPortletHelperUtil.getUserId());
        currentCart = null;
        currentCartOwner = null;
    }
}
