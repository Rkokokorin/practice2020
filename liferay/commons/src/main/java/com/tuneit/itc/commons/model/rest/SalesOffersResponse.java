package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class SalesOffersResponse {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("WarehouseId")
    private String warehouseId;
    @JsonProperty("CurrencyId")
    private String currencyId;
    @JsonProperty("ВариантыПоставки")
    private List<SalesOffer> salesOffers;
}
