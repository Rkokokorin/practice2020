package com.tuneit.itc.commons.jsf;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ByIdConverter<T> implements Converter {
    private final Map<String, T> byIds;
    private final Function<T, String> toIdMapper;

    public ByIdConverter(Map<String, T> byIds, Function<T, String> toIdMapper) {
        this.byIds = byIds;
        this.toIdMapper = toIdMapper;
    }

    public ByIdConverter(Collection<T> collection, Function<T, String> toIdMapper) {
        this.byIds = collection.stream()
            .collect(Collectors.toMap(toIdMapper, x -> x, (a, b) -> a));
        this.toIdMapper = toIdMapper;
    }

    public void addAll(Collection<T> collection) {
        for (T element : collection) {
            String key = toIdMapper.apply(element);
            byIds.putIfAbsent(key, element);
        }
    }

    @Override
    public T getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        return byIds.get(s);
    }

    public T get(String key) {
        return byIds.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o == null) {
            return null;
        }
        return toIdMapper.apply((T) o);
    }
}
