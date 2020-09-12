package com.tuneit.itc.catalogue.model;

import lombok.Data;

@Data
public class Manufacturer {
    private final String id;
    private final String name;
    private final String fullName;
    private final String nameEn;
    private final String fullNameEn;
    private final long associatedProducts;
}
