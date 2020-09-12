package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class NameCodePair {
    @JsonProperty("Наименование")
    private String name;
    @JsonProperty("Код")
    private String code;
}
