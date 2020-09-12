package com.tuneit.itc.commons.model;

import lombok.Data;

import com.liferay.portal.kernel.model.User;

import com.tuneit.itc.commons.util.ExpandoColumnDefinition;

@Data
public class ExtendedUser {

    public static final ExpandoColumnDefinition<String> CURRENCY_CODE =
        new ExpandoColumnDefinition<>(String.class, "com.tuneit.itc.user.currency-code");
    public static final ExpandoColumnDefinition<String> WAREHOUSE_CODE =
        new ExpandoColumnDefinition<>(String.class, "com.tuneit.itc.user.warehouse-code");

    private final User user;

    public String getCurrencyCode() {
        return CURRENCY_CODE.getValue(user);
    }

    public void setCurrencyCode(String currencyCode) {
        CURRENCY_CODE.setValue(user, currencyCode);
    }

    public String getWarehouseCode() {
        return WAREHOUSE_CODE.getValue(user);
    }

    public void setWarehouseCode(String warehouseCode) {
        WAREHOUSE_CODE.setValue(user, warehouseCode);
    }

}
