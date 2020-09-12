package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class MarketPrice implements Serializable {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("КодНоменклатуры")
    private String nomenclatureCode;
    @JsonProperty("РыночнаяЦена")
    private Double marketPrice;
    @JsonProperty("РыночнаяЦенаОт")
    private Double marketPriceLower;
    @JsonProperty("ДатаРасчета")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private Date settlementDate;
    @JsonProperty("КодВалюты")
    private String currencyCode;
    @JsonProperty("НаименованиеВалюты")
    private String currencyName;
    @JsonProperty("ЦенаКлиента")
    private Double personalPrice;

    public Double getViewPrice() {
        return this.marketPriceLower;
    }
}
