package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class OrderStatusItem implements Serializable {
    @JsonProperty("Период")
    private Date date;
    @JsonProperty("ПозицияЗаказа")
    private String orderPosition;
    @JsonProperty("ТоварНаименование")
    private String productName;
    @JsonProperty("ТоварКодНСИ")
    private String productCode;
    @JsonProperty("Статус")
    private String status;
    @JsonProperty("Количество")
    private Long amount;
    @JsonProperty("СостояниеПоставки")
    private String deliveryStatus;
    @JsonProperty("КатегорияОтвета")
    private String responseCategory;
}
