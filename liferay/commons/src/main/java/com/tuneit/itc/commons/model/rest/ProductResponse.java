package com.tuneit.itc.commons.model.rest;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.tuneit.itc.commons.model.ITCProductReference;
import com.tuneit.itc.commons.model.cart.ProductOffer;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ProductResponse extends ResponseBase {

    private Hits<ProductHitSource> hits;

    @Data
    public static class ProductHitSource implements ITCProductReference {
        @JsonProperty("Код")
        private String code;
        @JsonProperty("Наименование")
        private String name;
        @JsonProperty("НаименованиеПолное")
        private String fullName;
        @JsonProperty("НаименованиеКраткое")
        private String shortName;
        @JsonProperty("Описание")
        private String description;
        @JsonProperty("НаименованиеENG")
        private String nameEng;
        @JsonProperty("НаименованиеКраткоеENG")
        private String shortNameEng;
        @JsonProperty("НаименованиеПолноеENG")
        private String fullNameEng;
        @JsonProperty("СерияКоллекция")
        private String series;
        @JsonProperty("ОписаниеENG")
        private String descriptionEng;
        @JsonProperty("Артикул")
        private String vendorCode;
        @JsonProperty("Артикул2")
        private String secondVendorCode;
        @JsonProperty("ВидНоменклатуры")
        private NameCodePair nomenclatureType;
        @JsonProperty("Производитель")
        private NameCodePair manufacturer;
        @JsonProperty("ЕдиницаИзмерения")
        private NameCodePair unit;
        @JsonProperty("СнятоСПроизводства")
        private boolean outOfProduction;
        @JsonProperty("СнятоСПроизводстваДата")
        private Date outOfProductionDate;
        @JsonProperty("ГруппаТоваров")
        private NameCodePair productGroup;
        @JsonProperty("ПрисоединенныеФайлы")
        private List<AttachedFile> attachedFiles;
        @JsonProperty("ДополнительныеРеквизиты")
        private List<ProductFeature> features;
        @JsonProperty("АналогиНоменклатуры")
        private List<ProductReplacement> replacements;
        @JsonProperty("СертификатыНоменклатуры")
        private List<ProductCertificate> certificates;

        @Override
        public String getProductId() {
            return getCode();
        }

        @Override
        public ProductOffer getSelectedOffer() {
            return null;
        }

        public boolean hasCertificates() {
            return certificates != null && !certificates.isEmpty();
        }

        public String getDisplayName() {
            if (this.getShortName() != null && !this.getShortName().isBlank()) {
                return this.getShortName();
            } else if (this.getShortNameEng() != null && !this.getShortNameEng().isBlank()) {
                return this.getShortNameEng();
            } else {
                return this.getNomenclatureType().getName();
            }
        }


        @Data
        public static class ProductFeature {
            @JsonProperty("ИмяРеквизита")
            private String name;
            @JsonProperty("КодРеквизита")
            private String code;
            @JsonProperty("ЗначениеКод")
            private String strValue;
            @JsonProperty("ЗначениеБулево")
            private Boolean boolValue;
            @JsonProperty("ЗначениеЧисло")
            private Integer intValue;
            @JsonProperty("ЗначениеОт")
            private Integer valueFrom;
            @JsonProperty("ЗначениеДо")
            private Integer valueTo;
            @JsonProperty("ЗначениеНаименование")
            private String valueName;
            @JsonProperty("ЕдиницаИзмеренияРеквизита")
            private String valueUnit;

            public Object getValue() {
                if (valueName != null && !valueName.isBlank()) {
                    return valueName;
                } else if (valueFrom != null && valueTo != null) {
                    return "от " + valueFrom + " до " + valueTo;
                } else if (boolValue != null) {
                    return boolValue ? "да" : "нет";
                } else if (intValue != null) {
                    return intValue;
                } else if (strValue != null) {
                    return strValue;
                } else {
                    return null;
                }
            }

            public boolean hasValueUnit() {
                return valueUnit != null && !valueUnit.isBlank();
            }

            public String getValueWithUnit() {
                if (hasValueUnit()) {
                    return getValue() + " " + getValueUnit();
                } else {
                    return getValue() + "";
                }
            }
        }


        @Data
        public static class ProductCertificate {
            @JsonProperty("НомерСертификата")
            private String number;
            @JsonProperty("БессрочныйСертификат")
            private Boolean perpetual;
            @JsonProperty("ДатаОкончанияСрокаДействияСертификата")
            private Date endDate;

            public String getFormattedDate() {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                return dateFormat.format(endDate);
            }
        }
    }

    @Data
    public static class ProductReplacement {
        @JsonProperty("СнятоСПроизводства")
        private boolean outOfProduction;
        @JsonProperty("Артикул")
        private String vendorCode;
        @JsonProperty("Производитель")
        private String manufacturer;
        @JsonProperty("ТипАналога")
        private String replacementType;
        @JsonProperty("Код")
        private String productCode;
    }

}
