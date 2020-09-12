package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class PackingUnit {
    @JsonProperty("Код")
    private String code;
    @JsonProperty("Наименование")
    private String name;
    @JsonProperty("НаименованиеПолное")
    private String fullName;
    @JsonProperty("Коэффициент")
    private Integer multiplier;
    @JsonProperty("БазоваяЕдиницаИзмерения")
    private PackingUnit basePackingUnit;
}
