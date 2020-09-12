package com.tuneit.itc.cart;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class Views {
    public static final String VIEW = "/WEB-INF/views/view.xhtml";
    public static final String CART_VIEW = "/WEB-INF/views/cart-view.xhtml";
    public static final String SAVED = "/WEB-INF/views/saved-carts.xhtml";
    public static final String DELETED = "/WEB-INF/views/deleted-carts.xhtml";
    public static final String CLONE = "/WEB-INF/views/clone-cart.xhtml";
    public static final String COLLEAGUES = "/WEB-INF/views/colleagues-carts.xhtml";
    public static final String ASSISTANCE = "/WEB-INF/views/cart-assistance.xhtml";

    public String getView() {
        return VIEW;
    }

    public String getClone() {
        return CLONE;
    }

    public String getColleagues() {
        return COLLEAGUES;
    }

    public String getSaved() {
        return SAVED;
    }

    public String getDeleted() {
        return DELETED;
    }

    public String getCartView() {
        return CART_VIEW;
    }

    public String getAssistance() {
        return ASSISTANCE;
    }
}
