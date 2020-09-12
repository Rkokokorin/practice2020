package com.tuneit.itc.cart;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class RequestParams {
    public static final String CART_ID_PARAM = "cartId";
    public static final String USER_ID_PARAM = "userId";

    public String getCartIdParam() {
        return CART_ID_PARAM;
    }

    public String getUserIdParam() {
        return USER_ID_PARAM;
    }
}
