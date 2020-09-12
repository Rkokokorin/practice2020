package com.tuneit.itc.commons.model.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.tuneit.itc.commons.model.rest.ProductResponse;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrder {
    private Long amount;
    private String name;
    private String imageUrl;
    private double totalPrice;
    private String vcode;
    private String pcode;
    private String vendor;
    private CartPosition original;
    private ProductResponse.ProductHitSource source;

    public boolean isHasProduct() {
        return original.getExternalProductId() != null && !original.getExternalProductId().isBlank();
    }
}
