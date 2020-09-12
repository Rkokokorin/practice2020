package com.tuneit.itc.cart;

import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.tuneit.itc.commons.util.HttpUtil;

@Data
@ManagedBean
@ViewScoped
public class CartViewBean {
    @ManagedProperty("#{cartLoaderBean}")
    private CartLoaderBean cartBean;

    @PostConstruct
    public void init() {
        String cartIdParam = HttpUtil.getRequestParam(RequestParams.CART_ID_PARAM);
        try {
            long cartId = Long.parseLong(cartIdParam);
            cartBean.loadCart(cartId);
        } catch (NumberFormatException ignored) {

        }

    }
}
