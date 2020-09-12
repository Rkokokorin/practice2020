package com.tuneit.itc.commons.model.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ManufacturerResponse extends ResponseBase {

    private Hits<ManufacturerHitSource> hits;

    @Data
    public static class ManufacturerHitSource {
        @JsonProperty("Наименование")
        private String name;
        @JsonProperty("НаименованиеПолное")
        private String fullName;
        @JsonProperty("НаименованиеENG")
        private String nameEng;
        @JsonProperty("НаименованиеПолноеENG")
        private String fullNameEng;
        @JsonProperty("Код")
        private String code;
        @JsonProperty("КоличествоСоотносящейсяНоменклатуры")
        private long associatedProducts;
        @JsonProperty("ПорядокСортировки")
        private long sortingOrder;
    }
}
