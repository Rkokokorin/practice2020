package com.tuneit.itc.favorites;

import lombok.Data;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.RoleChecker;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.cart.Cart;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.SalesOffer;
import com.tuneit.itc.commons.model.rest.SalesOffersResponse;
import com.tuneit.itc.commons.service.cart.CartPositionService;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.util.LiferayUtil;

@Data
@ManagedBean
@ViewScoped
public class CartBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(CartBean.class);

    @ManagedProperty("#{cartPositionService}")
    private CartPositionService cartPositionService;
    @ManagedProperty("#{cartService}")
    private CartService cartService;

    private Cart currentCart;

    private ExtendedOrganization partner;
    private long userId;
    private long count = 1L;

    @PostConstruct
    private void init() {
        userId = LiferayPortletHelperUtil.getUserId();
        currentCart = cartService.findCurrentForUser(userId);
        partner = LiferayUtil.findContractorForUser(userId);
    }

    public void addToCart(ProductResponse.ProductHitSource product, SalesOffer offer,
                          SalesOffersResponse salesOffersResponse) {
        try {
            currentCart = cartService.findCurrentForUser(userId);
            if (currentCart == null) {
                currentCart = cartService.createInitialCart(partner, userId);
            }
            String warehouseId = null;
            String currencyId = null;
            if (salesOffersResponse != null) {
                warehouseId = salesOffersResponse.getWarehouseId();
                currencyId = salesOffersResponse.getCurrencyId();
            }
            cartPositionService.addToCart(currentCart, product, count, offer, warehouseId, currencyId);
            var prevCount = count;
            count = 1;
            log.debug("Successfully added {0}", product);
            LocalizedFacesMessage.infoFormat("itc.catalogue.product.add-success",
                new Object[] {prevCount, product.getNomenclatureType().getName(), product.getName()});
        } catch (Exception e) {
            log.error(e);
            LocalizedFacesMessage.error("itc.catalogue.product.add-error");
        }
    }

    public void addToCart(ProductResponse.ProductHitSource product) {
        addToCart(product, null, null);
    }

    public boolean isContractorEmployee() {
        return RoleChecker.hasAnyGroupRole(userId, RoleConstants.CONTRACTOR_EMPLOYEE, RoleConstants.CONTRACTOR_ADMIN);
    }

    public boolean isSalesManager() {
        return RoleChecker.hasAnyGlobalRole(LiferayPortletHelperUtil.getUserId(),
            RoleConstants.SALES_MANAGER, RoleConstants.SALES_DEPARTMENT_HEAD);
    }
}
