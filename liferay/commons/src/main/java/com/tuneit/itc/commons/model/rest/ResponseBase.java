package com.tuneit.itc.commons.model.rest;

import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ResponseBase {
    private int took;
    private boolean timedOut;

    @Data
    public static class Hits<T> {
        private long maxScore;
        private Total total;
        private List<Hit<T>> hits;
    }

    @Data
    public static class Hit<T> {
        @JsonProperty("_id")
        private String id;
        @JsonProperty("_source")
        private T source;
    }

    @Data
    public static class Total {
        private long value;
        private String relation;
    }
}
