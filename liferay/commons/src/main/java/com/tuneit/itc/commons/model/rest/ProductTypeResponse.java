package com.tuneit.itc.commons.model.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductTypeResponse extends ResponseBase {

    private Hits<ProductTypeHitSource> hits;

    @Data
    public static class ProductTypeHitSource {
        @JsonProperty("Наименование")
        private String name;
        @JsonProperty("НаименованиеENG")
        private String nameEng;
        @JsonProperty("ЭтоГруппа")
        private boolean group;
        @JsonProperty("Код")
        private String code;
        @JsonProperty("Родитель")
        private String parent;
        @JsonProperty("КоличествоСоотносящейсяНоменклатуры")
        private long associatedProducts;
        @JsonProperty("ПорядокСортировки")
        private long sortingOrder;
    }
}
