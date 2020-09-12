package com.tuneit.itc.cart;


import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@Data
@ViewScoped
@ManagedBean
public class DeletedCartsBean {

    @ManagedProperty("#{cartsListBean}")
    private CartsListBean cartsListBean;

    @PostConstruct
    public void init() {
        cartsListBean.setViewMode(CartsListBean.ViewMode.DELETED);
        cartsListBean.loadCarts();
    }
}
