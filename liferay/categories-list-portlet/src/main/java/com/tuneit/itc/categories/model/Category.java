package com.tuneit.itc.categories.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Category {
    private final String name;
    private final String id;
    private long associatedProducts;
    private long sortOrder;
    private final List<Category> subCategories = new ArrayList<>();
    private final List<CategoryItem> categoryItems = new ArrayList<>();

    public Category(String name, String id, long associatedProducts, long sortOrder) {
        this.name = name;
        this.id = id;
        this.associatedProducts = associatedProducts;
        this.sortOrder = sortOrder;
    }

    public boolean isHasSubCategories() {
        return !subCategories.isEmpty();
    }

    public boolean isHasItems() {
        return !categoryItems.isEmpty();
    }

    public Category addSubCategory(Category category) {
        this.subCategories.add(category);
        return this;
    }

    public Category addItem(CategoryItem item) {
        this.categoryItems.add(item);
        return this;
    }
}
