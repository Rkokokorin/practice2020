package com.tuneit.itc.search.model;

import lombok.Data;

@Data
public class ProductType {
    private final String name;
    private final String id;
    private final String parentId;
    private final boolean group;
}
