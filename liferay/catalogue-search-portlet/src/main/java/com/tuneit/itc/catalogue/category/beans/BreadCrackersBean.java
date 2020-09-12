package com.tuneit.itc.catalogue.category.beans;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;

import com.tuneit.itc.catalogue.model.Category;
import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ProductTypeResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.LiferayUtil;

@Data
@ManagedBean
@ViewScoped
public class BreadCrackersBean implements Serializable {
    private static final Log log = LogFactoryUtil.getLog(BreadCrackersBean.class);
    @ManagedProperty("#{viewBean.rootCategories}")
    private Map<String, Category> rootCategories;
    @ManagedProperty("#{productViewBean.product}")
    private ProductResponse.ProductHitSource product;
    @ManagedProperty("#{requester.productTypesService}")
    private Requester.ProductTypesService productTypesService;
    private String catalogueBaseUrl;
    private String categoryUrl;
    private String categoryName;
    private String subcategoryUrl;
    private String subcategoryName;
    private String productUrl;
    private String productCode;
    private Map<String, ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource>> byIdMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
            catalogueBaseUrl = catalogueUrl.toString();
            catalogueBaseUrl = catalogueBaseUrl.substring(0, catalogueUrl.toString().indexOf('?'));
        } catch (PortalException e) {
            log.error("Can't get catalogue base url. ", e);
            return;
        }

        loadFromProduct();
    }


    private void loadFromProduct() {
        if (catalogueBaseUrl == null) {
            return;
        }
        if (product == null) {
            return;
        }

        productCode = product.getName();
        subcategoryName = product.getNomenclatureType().getName();
        String subcategoryId = product.getNomenclatureType().getCode();
        ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> productType = requestById(subcategoryId);
        if (productType != null) {
            String categoryId = productType.getSource().getParent();
            var category = rootCategories.get(categoryId);
            if (category == null) {
                return;
            }
            categoryId = category.getId();
            if (categoryId == null) {
                return;
            }
            categoryUrl = catalogueBaseUrl + "?"
                + ParamNameConstants.CATEGORY_GROUP + "=" + categoryId;
            categoryName = category.getName();
        } else {
            String categoryId = product.getProductGroup().getCode();
            if (categoryId == null) {
                return;
            }
            categoryUrl = catalogueBaseUrl + "?"
                + ParamNameConstants.CATEGORY_GROUP + "=" + categoryId;
            categoryName = product.getProductGroup().getName();
        }

        if (subcategoryId == null) {
            return;
        }
        subcategoryUrl = categoryUrl + "&"
            + ParamNameConstants.CATEGORY_SUBGROUP + "=" + subcategoryId;

        String productId = product.getCode();
        if (productId == null) {
            return;
        }
        productUrl = subcategoryUrl + "&"
            + ParamNameConstants.PRODUCT + "=" + productId;
    }

    public boolean getSubcategoryUrlHidden() {
        if (byIdMap.get(product.getNomenclatureType().getCode()) == null) {
            byIdMap.put(product.getNomenclatureType().getCode(), requestById(product.getNomenclatureType().getCode()));
        }
        ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> productType =
            byIdMap.get(product.getNomenclatureType().getCode());

        if (productType == null || productType.getSource() == null) {
            return true;
        }
        return rootCategories.get(productType.getSource().getParent()).isHidden() ||
            productType.getSource().getSortingOrder() < 0;
    }

    public boolean getCategoryUrlHidden() {
        if (byIdMap.get(product.getNomenclatureType().getCode()) == null) {
            byIdMap.put(product.getNomenclatureType().getCode(), requestById(product.getNomenclatureType().getCode()));
        }
        ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> productType =
            byIdMap.get(product.getNomenclatureType().getCode());


        if (productType == null || productType.getSource() == null) {
            return true;
        }
        return rootCategories.get(productType.getSource().getParent()).isHidden();
    }

    private ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> requestById(String id) {
        try {
            Response<ProductTypeResponse> response = productTypesService.getById(id).execute();
            if (response.code() != 200) {
                log.error("Can't fetch product type by id. Response code is " + response.code()
                    + ". Message " + response.message());
                return null;
            }
            ProductTypeResponse body = response.body();
            if (body == null) {
                log.error("Can't fetch product type. Response body is null");
                return null;
            }
            var parentResponseHits = body.getHits().getHits();
            if (parentResponseHits.isEmpty()) {
                log.error("Can't fetch product type. Hits is empty");
                return null;
            }
            return parentResponseHits.get(0);
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }
}
