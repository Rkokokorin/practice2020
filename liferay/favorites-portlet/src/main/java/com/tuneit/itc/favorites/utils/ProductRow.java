package com.tuneit.itc.favorites.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.tuneit.itc.commons.model.ComparisonProduct;
import com.tuneit.itc.commons.model.FavoriteProduct;
import com.tuneit.itc.commons.model.rest.ProductResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRow {
    private ProductResponse.ProductHitSource product;
    private FavoriteProduct favorite;
    private ComparisonProduct comparison;
    private int counter = 0;

    public boolean isFavorite() {
        return favorite != null;
    }

    public boolean isComparison() {
        return comparison != null;
    }


}
