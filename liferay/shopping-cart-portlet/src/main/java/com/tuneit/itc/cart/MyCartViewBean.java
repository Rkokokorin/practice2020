package com.tuneit.itc.cart;

import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@Data
@ManagedBean
@ViewScoped
public class MyCartViewBean {
    @ManagedProperty("#{cartLoaderBean}")
    private CartLoaderBean cartBean;

    @PostConstruct
    public void init() {
        cartBean.loadCart();
    }
}
