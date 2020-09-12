package com.tuneit.itc.commons.util;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.model.User;

import com.tuneit.itc.commons.Pair;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.ITCProductReference;
import com.tuneit.itc.commons.model.cart.ProductOffer;
import com.tuneit.itc.commons.model.rest.SalesOffersResponse;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.rest.Requester;

public class OffersUtil {

    private static Logger log = LoggerFactory.getLogger(OffersUtil.class);

    private OffersUtil() {
        throw new IllegalStateException("This is an utility class");
    }

    public static String getCustomerId() {
        User user = LiferayPortletHelperUtil.getUser();
        ExtendedOrganization contractorForUser = LiferayUtil.findContractorForUser(user.getUserId());
        return contractorForUser == null ? null : contractorForUser.getBackOfficeCode();
    }

    public static SalesOffersResponse loadOffers(String productId,
                                                 Requester.SalesOffersService salesOffersService,
                                                 Currency preferredCurrency, Warehouse warehouse) {
        if (log.isDebugEnabled()) {
            log.debug("Load offers for warehouse {0}, currency {1}, product {2}", warehouse,
                preferredCurrency, productId);
        }

        String customerId = getCustomerId();

        Call<List<SalesOffersResponse>> call;
        if (customerId == null) {
            call = salesOffersService.get(productId,
                    warehouse.getCode(), preferredCurrency.getId());
        } else {
            call = salesOffersService.getWithUserData(productId,
                    warehouse.getCode(), preferredCurrency.getId(), customerId);
        }

        List<SalesOffersResponse> salesOffersResponses = processCall(call);
        if (salesOffersResponses == null || salesOffersResponses.isEmpty()) {
            return null;
        }
        return salesOffersResponses.get(0);
    }

    public static List<SalesOffersResponse> loadOffers(List<? extends ITCProductReference> products,
                                                       Requester.SalesOffersService salesOffersService,
                                                       Currency preferredCurrency, Warehouse selectedWarehouse) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
        List<Pair<String, String>> productIds = products.stream()
            .filter(prod -> prod.getProductId() != null)
            .map(prod -> {
                ProductOffer offer = prod.getSelectedOffer();
                if (offer != null && offer.getOfferId() != null) {
                    if (Objects.equals(offer.getCurrencyCode(), preferredCurrency.getId())
                        && Objects.equals(offer.getWarehouseId(), selectedWarehouse.getCode())) {
                        return Pair.of(offer.getQueryId().toString(), prod.getProductId());
                    } else {
                        return Pair.of((String) null, prod.getProductId());
                    }
                }
                return Pair.of((String) null, prod.getProductId());
            })
            .collect(Collectors.toList());
        log.debug("Product ids {0}", productIds);
        log.trace("Warehouse: {0}, currency: {1}", selectedWarehouse, preferredCurrency);

        String customerId = getCustomerId();

        Call<List<SalesOffersResponse>> offersCall;
        if (customerId == null) {
            offersCall = salesOffersService.getWithSelected(productIds,
                    selectedWarehouse.getCode(), preferredCurrency.getId());
        } else {
            offersCall = salesOffersService.getWithSelectedAndUserData(productIds,
                    selectedWarehouse.getCode(), preferredCurrency.getId(), customerId);
        }
        return processCall(offersCall);
    }

    private static List<SalesOffersResponse> processCall(Call<List<SalesOffersResponse>> offersCall) {
        Response<List<SalesOffersResponse>> offersExecute;
        try {
            offersExecute = offersCall.execute();
        } catch (IOException ioe) {
            log.error("Unexpected error while load offers: {0}", ioe.getMessage());
            return null;
        }
        List<SalesOffersResponse> offersBody = offersExecute.body();
        if (!offersExecute.isSuccessful() || offersBody == null) {
            ResponseBody errorBody = offersExecute.errorBody();

            String errorString = null;
            try {
                errorString = errorBody == null ? null : errorBody.string();
            } catch (IOException ioe) {
                log.error("Can not get error body! {0}", ioe.getMessage());
            }
            log.error("Error in service! {0} {1}", offersExecute.code(), errorString);
            return null;
        }
        log.trace("Offers: {0}", offersBody);
        List<SalesOffersResponse> createdOffers = offersBody.stream()
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        for (SalesOffersResponse offer : createdOffers) {
            offer.getSalesOffers().removeIf(salesOffer -> salesOffer.getSalesOfferId() == null);

        }
        return createdOffers;
    }
}
