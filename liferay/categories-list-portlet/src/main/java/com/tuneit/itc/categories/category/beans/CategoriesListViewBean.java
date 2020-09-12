package com.tuneit.itc.categories.category.beans;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;

import com.tuneit.itc.categories.model.Category;
import com.tuneit.itc.categories.model.CategoryItem;
import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.rest.ProductTypeResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.LiferayUtil;

@Data
@ManagedBean
@ViewScoped
public class CategoriesListViewBean {
    private List<Category> categories = new ArrayList<>();
    private Map<String, Category> rootCategories = new HashMap<>();
    @ManagedProperty("#{requester.productTypesService}")
    private Requester.ProductTypesService productTypesService;

    private Log log = LogFactoryUtil.getLog(this.getClass());
    private String categoryParamName;
    private String subcategoryParamName;
    private String catalogueBaseUrl = "/catalogue";

    @PostConstruct
    public void init() {
        categoryParamName = ParamNameConstants.CATEGORY_GROUP;
        subcategoryParamName = ParamNameConstants.CATEGORY_SUBGROUP;
        long size = 6000;
        long from = 0;

        var productTypeHitSourceHits = requestTypes(size, from);
        if (productTypeHitSourceHits != null) {
            var hits = productTypeHitSourceHits.getHits();
            long total = productTypeHitSourceHits.getTotal().getValue();

            if (total > size) {
                from = size;
                size = total - size;
                var allTypes = requestTypes(size, from);
                if (allTypes != null) {
                    hits.addAll(allTypes.getHits());
                }
            }

            hits.sort((t1, t2) -> t1.getSource().isGroup() == t2.getSource().isGroup()
                ? 0 : (t2.getSource().isGroup() ? 1 : -1));
            for (ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> hit : hits) {
                ProductTypeResponse.ProductTypeHitSource source = hit.getSource();
                if (source.isGroup()) {
                    Category group = rootCategories.get(source.getCode());
                    if (group == null) {
                        group = new Category(source.getName().trim(), source.getCode(),
                            source.getAssociatedProducts(), source.getSortingOrder());
                        rootCategories.put(source.getCode(), group);
                        categories.add(group);
                    }
                } else {
                    if (source.getParent().isBlank()) {
                        Category cat = new Category(source.getName().trim(), source.getCode(),
                            source.getAssociatedProducts(), source.getSortingOrder()
                        );
                        rootCategories.put(source.getCode(), cat);
                        categories.add(cat);
                    } else {
                        CategoryItem item = new CategoryItem(source.getName().trim(), source.getCode(),
                            source.getAssociatedProducts(), source.getSortingOrder()
                        );
                        String parent = source.getParent();
                        Category group = rootCategories.get(parent);
                        if (group == null) {
                            var parentResponse = requestById(parent);
                            if (parentResponse != null) {
                                group = new Category(parentResponse.getSource().getName().trim(),
                                    source.getParent(), source.getAssociatedProducts(), source.getSortingOrder()
                                );
                                group.addItem(item);
                                rootCategories.put(parentResponse.getSource().getCode(), group);
                                categories.add(group);
                            }
                        } else {
                            group.addItem(item);
                        }
                    }
                }
            }
            categories.removeIf(category -> !category.isHasItems() || category.getSortOrder() < 0);
            categories.sort(Comparator.comparing(Category::getName));
            for (var cat : categories) {
                cat.getCategoryItems().removeIf(categoryItem -> categoryItem.getSortOrder() < 0);
                cat.getCategoryItems().sort(Comparator.comparing(CategoryItem::getName));
            }

            try {
                LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
                catalogueBaseUrl = catalogueUrl.toString();
                catalogueBaseUrl = catalogueBaseUrl.substring(0, catalogueUrl.toString().indexOf('?'));
            } catch (PortalException e) {
                log.error("Can't get catalogue base url. " + e.getMessage());
                log.debug(e);
            }
        }
    }

    private ResponseBase.Hits<ProductTypeResponse.ProductTypeHitSource> requestTypes(long size, long from) {
        Response<ProductTypeResponse> response;
        try {
            response = productTypesService.getPage(from, size).execute();
            if (response.code() != 200) {
                log.error("Can't fetch product types. Response code is " + response.code()
                    + ". Message " + response.message());
                return null;
            }
            ProductTypeResponse body = response.body();
            if (body == null) {
                log.error("Can't fetch product types. Response body is null");
                return null;
            }
            return body.getHits();
        } catch (IOException e) {
            log.error("Can't fetch product types. " + e.getMessage());
            log.debug(e);
        }
        return null;
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
            log.error("Can not fetch product types " + e.getMessage());
            log.debug(e);
        }
        return null;
    }
}
