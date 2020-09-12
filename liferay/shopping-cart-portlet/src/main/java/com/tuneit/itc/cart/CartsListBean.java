package com.tuneit.itc.cart;


import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.cart.model.CartWithProductsInfo;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.cart.CartPositionService;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.service.rest.Requester;

@Data
@ViewScoped
@ManagedBean
public class CartsListBean {

    @ManagedProperty("#{cartService}")
    private CartService cartService;
    @ManagedProperty("#{cartPositionService}")
    private CartPositionService cartPositionService;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{cartUtilBean}")
    private CartUtilBean cartUtilBean;

    private List<CartService.CartWithPositionsCount> viewCarts;

    private long userId;
    private long currentUserId;
    private ViewMode viewMode;
    private boolean hasViewAccess = true;
    private Logger log = LoggerFactory.getLogger(CartsListBean.class);
    private List<CartWithProductsInfo> infoCarts;

    private Currency preferredCurrency;

    @PostConstruct
    public void init() {
        this.viewMode = ViewMode.ACTIVE;
        this.userId = LiferayPortletHelperUtil.getUserId();
        this.currentUserId = userId;
        Map<String, String> requestParameters = FacesContext.getCurrentInstance()
            .getExternalContext().getRequestParameterMap();
        String userIdParam = requestParameters.get(RequestParams.USER_ID_PARAM);
        if (userIdParam != null) {
            try {
                this.userId = Long.parseLong(userIdParam);
            } catch (NumberFormatException exc) {
                LogFactoryUtil.getLog(CartsListBean.class).info("Parameter is not a number " + userIdParam);
            }
        }
        this.hasViewAccess = hasViewAccess();
        if (LiferayPortletHelperUtil.getThemeDisplay().isSignedIn()) {
            preferredCurrency = currencyService.getUserCurrency();
            log.debug("Preferred currency is {0}", preferredCurrency);
        }
        loadCarts();
    }

    public void loadCarts() {
        if (viewMode == null) {
            throw new IllegalStateException("Can not load carts when viewMode is null");
        }
        if (hasViewAccess) {
            switch (viewMode) {
                case ACTIVE:
                    this.viewCarts = cartService.findCartsForUser(this.userId, true);
                    break;
                case DELETED:
                    this.viewCarts = cartService.findCartsForUser(this.userId, false);
                    break;
                default:
                    assert false : "Unexpected branch";

            }
        }
        infoCarts = viewCarts.stream()
            .map(cart -> {
                CartWithProductsInfo cartWithProductsInfo = new CartWithProductsInfo();
                cartWithProductsInfo.setCart(cart);
                return cartWithProductsInfo;
            })
            .collect(Collectors.toList());
        infoCarts = cartUtilBean.loadCartsOrders(viewCarts);
    }

    public void updateCurrency() {
        preferredCurrency = currencyService.getUserCurrency();
    }

    private boolean hasViewAccess() {
        //TODO: additional logic (check if user in the same organization, is admin and so on)
        return userId == currentUserId;
    }


    public enum ViewMode {
        DELETED,
        ACTIVE,
    }
}
