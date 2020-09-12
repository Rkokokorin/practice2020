package com.tuneit.itc.cart;


import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

@Data
@ViewScoped
@ManagedBean
public class SavedCartsBean {

    private final Logger log = LoggerFactory.getLogger(SavedCartsBean.class);
    @ManagedProperty("#{cartsListBean}")
    private CartsListBean cartsListBean;

    @PostConstruct
    public void init() {
        cartsListBean.setViewMode(CartsListBean.ViewMode.ACTIVE);
        cartsListBean.loadCarts();
        log.debug("Carts list: {0}", cartsListBean);
    }
}
