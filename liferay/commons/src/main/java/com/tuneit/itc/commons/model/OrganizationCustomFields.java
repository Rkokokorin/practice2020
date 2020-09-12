package com.tuneit.itc.commons.model;

import com.tuneit.itc.commons.util.ExpandoColumnDefinition;

public class OrganizationCustomFields {
    /**
     * Taxpayer's identification number.
     */
    public static final ExpandoColumnDefinition<String> TIN
        = new ExpandoColumnDefinition<>(String.class, "com.tuneit.itc.organization.inn");
    /**
     * Main state registration number.
     */
    public static final ExpandoColumnDefinition<String> MSRN
        = new ExpandoColumnDefinition<>(String.class, "com.tuneit.itc.organization.msrn");

    public static final ExpandoColumnDefinition<Boolean> IS_JURIDICAL_PERSON
        = new ExpandoColumnDefinition<>(Boolean.class, "com.tuneint.itc.organization.is_juridical");

    public static final ExpandoColumnDefinition<String> COMPANY_SHORT_NAME
        = new ExpandoColumnDefinition<>(String.class, "com.tuneit.itc.organization.short_name");

    public static final ExpandoColumnDefinition<Boolean> COMPANY_IS_PROVIDER
        = new ExpandoColumnDefinition<>(Boolean.class, "com.tuneit.itc.organization.is_provider");

    public static final ExpandoColumnDefinition<Boolean> COMPANY_IS_BUYER
        = new ExpandoColumnDefinition<>(Boolean.class, "com.tuneit.itc.organization.is_buyer");

    public static final ExpandoColumnDefinition<String> COMPANY_BACK_OFFICE_CODE
        = new ExpandoColumnDefinition<>(String.class, "com.tuneit.itc.organization.back_office_code");

    public static final ExpandoColumnDefinition<Long> SYNTHETIC_ID
        = new ExpandoColumnDefinition<>(Long.class, "com.tuneit.itc.organization.synthetic_id");

    public static final ExpandoColumnDefinition<String> PHONE
        = new ExpandoColumnDefinition<>(String.class, "com.tuneit.itc.organization.phone");

    private OrganizationCustomFields() {
        throw new UnsupportedOperationException("This is an utility class!");
    }
}
