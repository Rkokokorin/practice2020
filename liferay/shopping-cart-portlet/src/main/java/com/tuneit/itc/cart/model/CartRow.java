package com.tuneit.itc.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.rest.PackingUnit;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.SalesOffer;
import com.tuneit.itc.commons.model.rest.SalesOffersResponse;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRow implements Serializable {
    private long amount;
    private CartPosition original;
    private ProductResponse.ProductHitSource source;
    private SalesOffer selectedOffer;
    private SalesOffersResponse salesOffer;
    private String displayName;

    public CartRow(CartPosition original, ProductResponse.ProductHitSource source) {
        this.original = original;
        this.source = source;
        this.amount = original.getCount();
    }

    public static CartRow fromCartPosition(CartPosition cartPosition,
                                           Map<String, ProductResponse.ProductHitSource> productsByExternalId) {
        return new CartRow(cartPosition, productsByExternalId.get(cartPosition.getExternalProductId()));
    }

    public double getPrice() {
        return selectedOffer == null ? 0 : selectedOffer.getPrice();
    }

    public double getTotalPrice() {
        if (selectedOffer == null) {
            return 0;
        } else if (selectedOffer.getPersonalPrice() > 0) {
            return selectedOffer.getPersonalPrice() * amount;
        } else {
            return selectedOffer.getPrice() * amount;
        }
    }

    public String getName() {
        return source == null ? null : source.getName();
    }

    public String getImageUrl() {
        return source == null ? null : WindowsPathNormalizer.getProductImageUrl(source);
    }

    public String getVcode() {
        return source == null ? null : source.getVendorCode();
    }

    public String getSecondVcode() {
        return source == null ? null : source.getSecondVendorCode();
    }

    public String getPcode() {
        return source == null ? null : source.getCode();
    }

    public String getVendor() {
        return source == null ? null : source.getManufacturer().getName();
    }

    public String getDisplayName() {
        if (source == null) {
            return original.getProductDescription();
        } else {
            return source.getDisplayName();
        }
    }

    public boolean isHasProduct() {
        return original.getExternalProductId() != null && !original.getExternalProductId().isBlank();
    }

    public Boolean displayBasePacking() {
        if (selectedOffer == null) {
            return false;
        }
        Optional<SalesOffer> selectedOffer = Optional.of(this.selectedOffer);
        Optional<PackingUnit> packingUnit = selectedOffer.map(SalesOffer::getPackingUnit);
        String packingUnitCode = packingUnit.map(PackingUnit::getCode).orElse(null);
        String basePackingUnit = packingUnit.map(PackingUnit::getBasePackingUnit)
            .map(PackingUnit::getCode).orElse(null);
        return !Objects.equals(packingUnitCode, basePackingUnit) ||
            packingUnit.map(PackingUnit::getMultiplier).map(mul -> mul > 1).orElse(false);
    }

}
