package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Warehouse {
    @JsonProperty("Код")
    private String code;
    @JsonProperty("Наименование")
    private String name;
    @JsonProperty("СтранаКод")
    private String countryCode;
    @JsonProperty("СтранаНаименование")
    private String countryName;
    @JsonProperty("ПорядокСортировки")
    private long sortOrder;
}
