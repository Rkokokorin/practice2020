package com.tuneit.itc.commons.model;

import com.tuneit.itc.commons.model.cart.ProductOffer;

public interface ITCProductReference {
    String getProductId();

    ProductOffer getSelectedOffer();
}
