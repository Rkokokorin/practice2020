package com.tuneit.itc.commons.model.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CurrencyResponse extends ResponseBase implements Serializable {
    private Hits<CurrencyHit> hits;

    @Data
    public static class CurrencyHit {
        @JsonProperty("Наименование")
        private String name;
        @JsonProperty("НаименованиеПолное")
        private String fullName;
        @JsonProperty("Код")
        private String code;
        @JsonProperty("ЗагружаетсяИзИнтернета")
        private boolean loadedFromInternet;
        @JsonProperty("Наценка")
        private long extraCharge;

    }
}
