package com.tuneit.itc.cart;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import com.tuneit.itc.cart.model.CartRow;
import com.tuneit.itc.commons.jsf.ByIdConverter;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.cart.Cart;
import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.service.cart.CartPositionService;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.CartUtil;
import com.tuneit.itc.commons.util.HttpUtil;
import com.tuneit.itc.commons.util.LiferayUtil;

@Data
@ManagedBean
@ViewScoped
public class CloneCartBean {

    private Cart selectedCart;
    private List<CartRow> orders;
    private List<CartRow> selectedOrders;

    @ManagedProperty("#{cartService}")
    private CartService cartService;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{cartPositionService}")
    private CartPositionService cartPositionService;

    private String cartName;
    private User cartOwner;

    private ByIdConverter<CartRow> orderConverter;
    private Log log = LogFactoryUtil.getLog(this.getClass());

    private boolean hasCloneAccess;

    private boolean wasCloned;

    @PostConstruct
    public void init() {
        this.selectedOrders = new ArrayList<>();
        String cartIdParam = HttpUtil.getRequestParam(RequestParams.CART_ID_PARAM);
        log.debug("Cart id: " + cartIdParam);
        try {
            long cartId = Long.parseLong(cartIdParam);
            this.selectedCart = cartService.find(cartId).orElse(null);
        } catch (NumberFormatException ignored) {

        }
        hasCloneAccess = canClone();
        wasCloned = false;
        loadOrders();
        fillCartName();
    }

    public void cloneCart() {
        log.debug(selectedOrders);
        long userId = LiferayPortletHelperUtil.getUserId();
        Cart currentCartForUser = cartService.findCurrentForUser(userId);
        if (currentCartForUser != null) {
            LocalizedFacesMessage.error("itc.cart.clone.current-exists");
            return;
        }
        try {
            List<CartPosition> clonedPositions = selectedOrders.stream()
                .map(CartRow::getOriginal)
                .collect(Collectors.toList());
            ExtendedOrganization contractorForUser = LiferayUtil.findContractorForUser(userId);
            cartService.cloneCart(contractorForUser, userId, clonedPositions, cartName);
            wasCloned = true;
            LocalizedFacesMessage.info("itc.cart.clone.success");
        } catch (Exception e) {
            log.warn("An exception during cart cloning", e);
            LocalizedFacesMessage.error("itc.cart.clone.exception");
        }

    }

    private boolean canClone() {
        long userId = LiferayPortletHelperUtil.getUserId();
        if (selectedCart == null || selectedCart.getOwnerId() == userId) {
            return true;
        }
        ExtendedOrganization contractorForUser = LiferayUtil.findContractorForUser(userId);
        ExtendedOrganization contractorForOwner = LiferayUtil.findContractorForUser(selectedCart.getOwnerId());
        return contractorForOwner != null && contractorForUser != null
            && Objects.equals(contractorForOwner.getBackOfficeCode(), contractorForUser.getBackOfficeCode());
    }

    private void loadOrders() {
        if (selectedCart == null) {
            log.warn("Selected cart is null");
            return;
        }
        List<CartPosition> positions = cartPositionService.findForCart(selectedCart);
        Map<String, ProductResponse.ProductHitSource> byExternalIds =
            CartUtil.fromCartPositions(productsService, positions);
        this.orders = positions.stream()
            .map(position -> CartRow.fromCartPosition(position, byExternalIds))
            .collect(Collectors.toList());
        this.selectedOrders = new ArrayList<>(this.orders);
        this.orderConverter = new ByIdConverter<>(this.orders, CartRow::getPcode);
    }

    private void fillCartName() {
        if (!hasCloneAccess || selectedCart == null) {
            return;
        }
        StringBuilder newCartName = new StringBuilder(LocalizedFacesMessage.i18n("itc.cart.clone.name-prefix"));
        if (selectedCart.getOwnerId() != LiferayPortletHelperUtil.getUserId()) {
            try {
                User ownerUser = UserLocalServiceUtil.getUser(selectedCart.getOwnerId());
                newCartName.append(" ")
                    .append(ownerUser.getFullName());
            } catch (PortalException e) {
                log.error(e);
            }
        }
        newCartName.append(": ")
            .append(selectedCart.getCartName());
        cartName = newCartName.toString();
    }
}
