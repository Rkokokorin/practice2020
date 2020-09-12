package com.tuneit.itc.catalogue.category.beans;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.catalogue.model.Category;
import com.tuneit.itc.commons.model.rest.ProductTypeResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.rest.Requester;

@Data
@ManagedBean
@ViewScoped
public class CategoriesListBean {
    private List<Category> categories = new ArrayList<>();
    private Map<String, Category> rootCategories = new HashMap<>();
    private Set<String> thirdLevelCategories = new HashSet<>();
    @ManagedProperty("#{requester.productTypesService}")
    private Requester.ProductTypesService productTypesService;

    private Log log = LogFactoryUtil.getLog(this.getClass());

    @PostConstruct
    public void init() {
        long size = 1000;
        long from = 0;

        var productTypeHitSourceHits = requestGroups(size, from);
        if (productTypeHitSourceHits != null) {
            var hits = productTypeHitSourceHits.getHits();
            long total = productTypeHitSourceHits.getTotal().getValue();

            if (total > size) {
                from = size;
                size = total - size;
                var allTypes = requestGroups(size, from);
                if (allTypes != null) {
                    hits.addAll(allTypes.getHits());
                }
            }

            for (ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> hit : hits) {
                ProductTypeResponse.ProductTypeHitSource source = hit.getSource();
                Category group = new Category(source.getName().trim(), source.getCode(),
                    source.getAssociatedProducts(), source.getSortingOrder() < 0, source.getParent(), false);
                rootCategories.put(source.getCode(), group);
                if (hit.getSource().getParent() != null && !hit.getSource().getParent().isBlank()) {
                    thirdLevelCategories.add(hit.getSource().getParent());
                }
            }
        }
        for (String thirdLevelCategory : thirdLevelCategories) {
            rootCategories.remove(thirdLevelCategory);
        }
        categories = new ArrayList<>(rootCategories.values());
        categories.removeIf(Category::isHidden);
        categories.sort(Comparator.comparing(Category::getName));
    }

    private ResponseBase.Hits<ProductTypeResponse.ProductTypeHitSource> requestGroups(long size, long from) {
        Response<ProductTypeResponse> response;
        try {
            response = productTypesService.getGroupPage(from, size).execute();
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
            log.error(e);
        }
        return null;
    }

    private ResponseBase.Hits<ProductTypeResponse.ProductTypeHitSource> requestGroupsWithQuery(String query,
                                                                                               long size, long from) {
        Response<ProductTypeResponse> response;
        try {
            if (query.isBlank()) {
                response = productTypesService.getGroupPageWithQuery(query, from, size).execute();
            } else {
                response = productTypesService.query(query, from, size).execute();
            }
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
            log.error(e);
        }
        return null;
    }

    public List<Category> searchGroup(String query) {
        long size = 1000;
        long from = 0;
        var productTypeHitSourceHits = requestGroupsWithQuery(query, size, from);
        List<Category> result = new ArrayList<>();
        if (productTypeHitSourceHits != null) {
            var hits = productTypeHitSourceHits.getHits();
            long total = productTypeHitSourceHits.getTotal().getValue();

            if (total > size) {
                from = size;
                size = total - size;
                var allTypes = requestGroups(size, from);
                if (allTypes != null) {
                    hits.addAll(allTypes.getHits());
                }
            }

            for (ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> hit : hits) {
                ProductTypeResponse.ProductTypeHitSource source = hit.getSource();
                Category group = new Category(source.getName().trim(), source.getCode(),
                    source.getAssociatedProducts(), source.getSortingOrder() < 0, source.getParent(),
                    !rootCategories.containsKey(source.getCode())
                );
                if (!thirdLevelCategories.contains(group.getId())) {
                    result.add(group);
                }
            }
        }
        return result;
    }

    private ResponseBase.Hits<ProductTypeResponse.ProductTypeHitSource>
            requestCategoriesForParentWIthQuery(String query, String parentId, long size, long from) {
        Response<ProductTypeResponse> response;
        try {
            response = productTypesService.getByParentWithQuery(query, parentId, from, size).execute();
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
            log.error(e);
        }
        return null;
    }

    public List<Category> searchSubcategory(String query, String parentId) {
        long size = 1000;
        long from = 0;
        var productTypeHitSourceHits = requestCategoriesForParentWIthQuery(query, parentId, size, from);
        List<Category> result = new ArrayList<>();
        if (productTypeHitSourceHits != null) {
            var hits = productTypeHitSourceHits.getHits();
            long total = productTypeHitSourceHits.getTotal().getValue();

            if (total > size) {
                from = size;
                size = total - size;
                var allTypes = requestGroups(size, from);
                if (allTypes != null) {
                    hits.addAll(allTypes.getHits());
                }
            }

            for (ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> hit : hits) {
                ProductTypeResponse.ProductTypeHitSource source = hit.getSource();
                Category sub = new Category(source.getName().trim(), source.getCode(), source.getAssociatedProducts(),
                    false, source.getParent(), true);
                result.add(sub);
            }
        }
        return result;
    }

}
