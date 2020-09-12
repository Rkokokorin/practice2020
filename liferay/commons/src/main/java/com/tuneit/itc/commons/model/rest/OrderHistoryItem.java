package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class OrderHistoryItem {
    @JsonProperty("Id")
    private String id;
    @JsonProperty("Дата")
    private Date date;
    @JsonProperty("Номер")
    private String number;
    @JsonProperty("Менеджер")
    private String manager;
    @JsonProperty("ВалютаДокумента")
    private String currency;
    @JsonProperty("СуммаДокумента")
    private Double price;
    @JsonProperty("Статус")
    private String status;
    @JsonProperty("ПризнакОплаты")
    private String payment;

    public Date getMonthAndYear() {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), zoneId);
        return Date.from(dateTime
            .with(TemporalAdjusters.firstDayOfMonth())
            .truncatedTo(ChronoUnit.DAYS)
            .atZone(zoneId).toInstant()
        );
    }

    @Data
    public static class DateWrapper {
        private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        private Date date;

        public DateWrapper(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return dateFormat.format(date);
        }
    }
}
