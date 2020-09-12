package com.tuneit.itc.catalogue.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import com.tuneit.itc.commons.model.rest.MarketPrice;
import com.tuneit.itc.commons.model.rest.ProductResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplacementProductView implements Serializable {
    private ProductResponse.ProductHitSource product;
    private MarketPrice price;
}
