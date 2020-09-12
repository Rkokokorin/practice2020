package com.tuneit.itc.catalogue.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

@Data
@EqualsAndHashCode(exclude = {"categoryItems"})
@NoArgsConstructor
public class Category {
    private String name;
    private String id;
    private long associatedProducts;
    private boolean hidden;
    private String parentId;
    private boolean subcategory;
    private Set<Category> categoryItems = new TreeSet<>(Comparator.comparing(Category::getName));

    public Category(String name, String id, long associatedProducts, boolean hidden,
                    String parentId, boolean subcategory) {
        this.name = name;
        this.id = id;
        this.associatedProducts = associatedProducts;
        this.hidden = hidden;
        this.parentId = parentId;
        this.subcategory = subcategory;
    }


    public boolean isHasItems() {
        return !categoryItems.isEmpty();
    }

    public Category addItem(Category item) {
        this.categoryItems.add(item);
        return this;
    }
}
