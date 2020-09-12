package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class SalesOffer {
    @JsonProperty("ИдентификаторЗапроса")
    private String requestId;
    @JsonProperty("ИдентификаторВариантаПоставки")
    private String salesOfferId;
    @JsonProperty("ИдентификаторПредложенияПоставщика")
    private String providerOfferId;
    @JsonProperty("Длительность")
    private Integer duration; //Длительность поставки в часах
    @JsonProperty("ЦенаВариантаПоставки")
    private Double price;
    @JsonProperty("Кратность")
    private Integer multiplicity;
    @JsonProperty("ДатаРасчета")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private Date calculationDate;
    @JsonProperty("УпаковкаЕдиницаИзмерения")
    private PackingUnit packingUnit;
    @JsonProperty("ОстатокНаСкладе")
    private Integer stockAmount;
    @JsonProperty("МинимальноеКоличествоКЗаказу")
    private Integer minimalOrder;
    @JsonProperty("ЦенаКлиента")
    private Double personalPrice;

    public Integer getDaysDuration() {
        if (duration == null) {
            return null;
        }
        return (int) TimeUnit.HOURS.toDays(duration);
    }
}
