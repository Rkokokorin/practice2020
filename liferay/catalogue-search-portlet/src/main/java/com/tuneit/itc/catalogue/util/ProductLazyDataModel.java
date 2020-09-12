package com.tuneit.itc.catalogue.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.catalogue.category.beans.ViewBean;
import com.tuneit.itc.commons.Pair;
import com.tuneit.itc.commons.model.ComparisonProduct;
import com.tuneit.itc.commons.model.FavoriteProduct;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.ComparisonProductService;
import com.tuneit.itc.commons.service.FavoriteProductService;
import com.tuneit.itc.commons.service.rest.Requester;

@AllArgsConstructor
public class ProductLazyDataModel extends LazyDataModel<ProductRow> {
    private final Log log = LogFactoryUtil.getLog(this.getClass());
    private final ViewBean.Filters filters;
    private Requester.ProductsService productsService;
    private ComparisonProductService comparisonProductService;
    private FavoriteProductService favoriteProductService;
    private long userId;
    @Getter
    private List<ProductRow> currentPage;

    private Integer lastFirst;
    private Integer lastPageSize;
    private ViewBean.Filters lastFilters;

    public ProductLazyDataModel(Requester.ProductsService productsService,
                                ComparisonProductService comparisonProductService,
                                FavoriteProductService favoriteProductService,
                                long userId, ViewBean.Filters filters, List<ProductRow> currentPage) {
        this.productsService = productsService;
        this.comparisonProductService = comparisonProductService;
        this.favoriteProductService = favoriteProductService;
        this.userId = userId;
        this.filters = filters;
        this.lastFilters = new ViewBean.Filters(filters);
        this.currentPage = currentPage;
    }

    @Override
    public List<ProductRow> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                                 Map<String, Object> f) {
        if (lastFirst != null && lastPageSize != null
            && (first == lastFirst && lastPageSize == pageSize) && lastFilters.equals(filters)) {
            return currentPage;
        }
        if (log.isTraceEnabled()) {
            log.trace("Call load", new Throwable());
            log.trace("Last filters: " + lastFilters);
        }
        sortField = sortField == null ? "id" : sortField;
        Pair<List<ProductRow>, Long> data = loadFromDb(first, pageSize, sortField, sortOrder, f);
        this.setRowCount(Math.min(100, Math.toIntExact(data.getSecond())));
        currentPage = data.getFirst();
        return data.getFirst();
    }

    private Pair<List<ProductRow>, Long> loadFromDb(int first, int pageSize,String sortField, SortOrder sortOrder,
                                                    Map<String, Object> f) {
        List<ProductResponse> body = null;
        try {
            if (filters.getPartNumberSearch()) {
                log.trace("Execute product service call");
                body = productsService
                    .getByVendorCodeAndProductTypeAndManufacturer(filters.getSearchQuery(),
                        first,
                        pageSize,
                        filters.getSelectedSubcategoryId().equals("") ?
                            filters.getSelectedCategoryId() : filters.getSelectedSubcategoryId(),
                        filters.getSelectedManufacturerId())
                    .execute().body();
            } else {
                log.trace("Execute product service call");
                body = productsService.queryAndProductTypeAndManufacturer(filters.getSearchQuery(),
                    first,
                    pageSize,
                    filters.getSelectedSubcategoryId().equals("") ?
                        filters.getSelectedCategoryId() : filters.getSelectedSubcategoryId(),
                    filters.getSelectedManufacturerId())
                    .execute().body();
            }
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage());
        }
        if (body == null || body.size() < 1 || body.get(0).getHits() == null) {
            return Pair.of(null, 0L);
        }
        List<ProductResponse.ProductHitSource> hits = body.get(0).getHits().getHits()
            .stream()
            .map(ResponseBase.Hit::getSource)
            .collect(Collectors.toList());
        List<String> productCodes = hits
            .stream()
            .map(ProductResponse.ProductHitSource::getCode)
            .collect(Collectors.toList());
        List<ComparisonProduct> comparisons = new ArrayList<>();
        List<FavoriteProduct> favorites = new ArrayList<>();
        if (!hits.isEmpty()) {
            comparisons = comparisonProductService.findAllForUserAndProductIdIn(userId, productCodes);
            favorites = favoriteProductService.findAllForUserAndProductIdIn(userId, productCodes);
        }
        List<ProductRow> results = new ArrayList<>();
        for (ProductResponse.ProductHitSource hit : hits) {
            ComparisonProduct comparison = null;
            FavoriteProduct favorite = null;
            String productCode = hit.getCode();
            comparison = comparisons.stream()
                .filter(comparisonProduct -> productCode.equals(comparisonProduct.getProductCode()))
                .findFirst().orElse(null);
            favorite = favorites.stream()
                .filter(favoriteProduct -> productCode.equals(favoriteProduct.getProductCode()))
                .findFirst().orElse(null);
            results.add(new ProductRow(hit, favorite, comparison, 1));
        }
        lastFirst = first;
        lastPageSize = pageSize;
        lastFilters = new ViewBean.Filters(filters);
        return Pair.of(results, body.get(0).getHits().getTotal().getValue());
    }

}
