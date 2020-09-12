package com.tuneit.itc.commons.util;

import lombok.Data;

import java.io.Serializable;

import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.portal.kernel.model.BaseModel;

@Data
public class ExpandoColumnDefinition<T extends Serializable> {
    private final Class<T> classType;
    private final int type;
    private final String name;

    public ExpandoColumnDefinition(Class<T> classType, String name) {
        this.name = name;
        this.classType = classType;
        if (classType == String.class) {
            this.type = ExpandoColumnConstants.STRING;
        } else if (classType == int.class || classType == Integer.class) {
            this.type = ExpandoColumnConstants.INTEGER;
        } else if (classType == boolean.class || classType == Boolean.class) {
            this.type = ExpandoColumnConstants.BOOLEAN;
        } else if (classType == long.class || classType == Long.class) {
            this.type = ExpandoColumnConstants.LONG;
        } else {
            throw new IllegalArgumentException("Type for class " + classType.getCanonicalName() + " is not defined");
        }
    }

    public T getValue(BaseModel<?> model) {
        return classType.cast(model.getExpandoBridge().getAttribute(getName()));
    }

    public void setValue(BaseModel<?> model, T value) {
        model.getExpandoBridge().setAttribute(getName(), value);
    }
}
