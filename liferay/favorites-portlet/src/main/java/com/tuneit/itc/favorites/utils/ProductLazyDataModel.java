package com.tuneit.itc.favorites.utils;


import lombok.AllArgsConstructor;
import lombok.Getter;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

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
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Requester.ProductsService productsService;
    private ComparisonProductService comparisonProductService;
    private FavoriteProductService favoriteProductService;
    private long userId;

    @Getter
    private List<ProductRow> currentPage;

    private Integer lastFirst;
    private Integer lastPageSize;

    public ProductLazyDataModel(Requester.ProductsService productsService,
                                ComparisonProductService comparisonProductService,
                                FavoriteProductService favoriteProductService,
                                long userId) {
        this.productsService = productsService;
        this.comparisonProductService = comparisonProductService;
        this.favoriteProductService = favoriteProductService;
        this.userId = userId;
        this.currentPage = new ArrayList<>();
    }

    @Override
    public List<ProductRow> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                                 Map<String, Object> filters) {

        if (log.isTraceEnabled()) {
            log.trace("Call load", new Throwable());
        }
        sortField = sortField == null ? "id" : sortField;
        Pair<List<ProductRow>, Long> data = loadFromDb(first, pageSize, sortField, sortOrder, filters);
        this.setRowCount(Math.min(100, Math.toIntExact(data.getSecond())));
        currentPage = data.getFirst();
        return data.getFirst();
    }

    private Pair<List<ProductRow>, Long> loadFromDb(int first, int pageSize, String sortField, SortOrder sortOrder,
                                                    Map<String, Object> filters) {
        final long count = favoriteProductService.countForUser(userId);
        List<FavoriteProduct> favoriteProducts = favoriteProductService.findForUser(userId,
            first / pageSize + 1, pageSize);
        if (favoriteProducts == null || favoriteProducts.size() < 1) {
            return Pair.of(null, 0L);
        }
        List<String> productCodes = favoriteProducts.stream().map(FavoriteProduct::getProductCode)
            .collect(Collectors.toList());
        List<ComparisonProduct> comparisonProducts = comparisonProductService
            .findAllForUserAndProductIdIn(userId, productCodes);
        List<ProductResponse.ProductHitSource> productsByExternalId = new ArrayList<>();
        try {

            Response<List<ProductResponse>> response = productsService
                .getByIds(productCodes)
                .execute();
            if (response.isSuccessful()) {
                var body = response.body();
                if (body != null) {
                    productsByExternalId = body
                        .stream()
                        .map(ProductResponse::getHits)
                        .map(ResponseBase.Hits::getHits)
                        .flatMap(List::stream)
                        .map(ResponseBase.Hit::getSource)
                        .collect(Collectors.toList());
                } else {
                    log.warn("Unexpected null body for ids {0}", productCodes);
                }
            } else {
                log.warn("Request to API was failed. Code is {}, body is {1}", response.code(),
                    (response.errorBody() != null ? response.errorBody().string() : null));
            }

        } catch (IOException exc) {
            log.warn("Unexpected IOException!", exc);
        }

        List<ProductRow> results = new ArrayList<>();
        for (ProductResponse.ProductHitSource hit : productsByExternalId) {
            ComparisonProduct comparison = null;
            FavoriteProduct favorite = null;
            String productCode = hit.getCode();
            comparison = comparisonProducts.stream()
                .filter(comparisonProduct -> productCode.equals(comparisonProduct.getProductCode()))
                .findFirst().orElse(null);
            favorite = favoriteProducts.stream()
                .filter(favoriteProduct -> productCode.equals(favoriteProduct.getProductCode()))
                .findFirst().orElse(null);
            results.add(new ProductRow(hit, favorite, comparison, 1));
        }
        lastFirst = first;
        lastPageSize = pageSize;
        return Pair.of(results, count);
    }

}
