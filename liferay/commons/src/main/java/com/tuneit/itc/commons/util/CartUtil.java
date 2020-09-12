package com.tuneit.itc.commons.util;

import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.rest.Requester;

public class CartUtil {
    private static final Logger log = LoggerFactory.getLogger(CartUtil.class);

    public static List<ProductResponse.ProductHitSource> getFromCartPositions(Requester.ProductsService productsService,
                                                                              List<CartPosition> cartPositions) {
        List<String> productIds = cartPositions
            .stream()
            .map(CartPosition::getExternalProductId)
            .distinct()
            .collect(Collectors.toList());
        log.debug("Product ids: {0}", productIds);
        List<ProductResponse.ProductHitSource> productsByExternalId = new ArrayList<>();
        try {
            Response<List<ProductResponse>> response = productsService
                .getByIds(productIds)
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
                    log.warn("Unexpected null body for ids {0}", productIds);
                }
            } else {
                log.warn("Request to API was failed. Code is {}, body is {1}", response.code(),
                    (response.errorBody() != null ? response.errorBody().string() : null));
            }

        } catch (IOException exc) {
            log.warn("Unexpected IOException!", exc);
        }
        return productsByExternalId;
    }

    public static Map<String, ProductResponse.ProductHitSource> fromCartPositions(
        Requester.ProductsService productsService, List<CartPosition> cartPositions) {
        return getFromCartPositions(productsService, cartPositions).stream()
            .collect(Collectors.toMap(ProductResponse.ProductHitSource::getCode, x -> x, (fst, snd) -> fst));
    }

    public static double computeTotal(List<CartPosition> orders, Currency currency, Warehouse warehouse) {
        String currencyId = Optional.ofNullable(currency).map(Currency::getId).orElse(null);
        String warehouseId = Optional.ofNullable(warehouse).map(Warehouse::getCode).orElse(null);
        if (orders == null) {
            return 0;
        }
        return orders.stream()
            .filter(cartPosition -> cartPosition.getSelectedOffer() != null)
            .filter(cartPosition -> Objects.equals(cartPosition.getCurrencyCode(), currencyId))
            .filter(cartPosition -> Objects.equals(cartPosition.getWarehouseId(), warehouseId))
            .mapToDouble(CartPosition::getTotalPrice)
            .sum();
    }

    private CartUtil() {
        throw new UnsupportedOperationException("This is an utility class!");
    }

}
