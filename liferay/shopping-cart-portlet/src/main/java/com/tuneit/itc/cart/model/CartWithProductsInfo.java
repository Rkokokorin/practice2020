package com.tuneit.itc.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.service.cart.CartService;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartWithProductsInfo {
    private CartService.CartWithPositionsCount cart;
    private List<ProductResponse.ProductHitSource> sources = new ArrayList<>();
}
