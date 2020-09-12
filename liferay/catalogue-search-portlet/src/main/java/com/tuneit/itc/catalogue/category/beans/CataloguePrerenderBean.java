package com.tuneit.itc.catalogue.category.beans;

import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.HttpUtil;

@Data
@ManagedBean
@ViewScoped
public class CataloguePrerenderBean {
    private final Logger logger = LoggerFactory.getLogger(CataloguePrerenderBean.class);
    private final String selectedProductIdParamName = ParamNameConstants.PRODUCT;
    private String selectedProductId;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;

    @PostConstruct
    public void init() {
        selectedProductId = HttpUtil.getRequestParam(ParamNameConstants.PRODUCT);
    }

    public String checkProductSelected() {
        logger.trace("Call catalogue pre render");
        if (selectedProductId != null && !selectedProductId.isBlank()) {
            return "/WEB-INF/views/product.xhtml?faces-redirect=true&"
                + selectedProductIdParamName
                + "=" + selectedProductId;
        }
        return "/WEB-INF/views/view.xhtml";
    }
}
