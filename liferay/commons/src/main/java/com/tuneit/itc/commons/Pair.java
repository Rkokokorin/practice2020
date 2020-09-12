package com.tuneit.itc.commons;

import lombok.Data;

@Data
public class Pair<F, S> {
    private final F first;
    private final S second;

    public static <FT, ST> Pair<FT, ST> of(FT first, ST second) {
        return new Pair<>(first, second);
    }
}
