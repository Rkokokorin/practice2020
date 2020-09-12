package com.tuneit.itc.categoriesmenu.model;

import lombok.Data;

@Data
public class CategoryItem {
    private final String name;
    private final String id;
    private final long associatedProducts;
    private final long sortOrder;
}
