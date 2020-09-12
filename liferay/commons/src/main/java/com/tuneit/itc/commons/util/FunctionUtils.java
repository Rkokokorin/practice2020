package com.tuneit.itc.commons.util;

import java.util.Map;

import com.tuneit.itc.commons.Pair;

public class FunctionUtils {

    private FunctionUtils() {
        throw new IllegalStateException("This is an utility class!");
    }

    public static <T> T id(T t) {
        return t;
    }

    public static <F, S> F fst(F fst, S snd) {
        return fst;
    }

    public static <F, S> F fst(Pair<F, S> pair) {
        return pair.getFirst();
    }

    public static <F, S> F fst(Map.Entry<F, S> pair) {
        return pair.getKey();
    }

    public static <F, S> S snd(F first, S second) {
        return second;
    }

    public static <F, S> S snd(Pair<F, S> pair) {
        return pair.getSecond();
    }

    public static <F, S> S snd(Map.Entry<F, S> entry) {
        return entry.getValue();
    }
}
