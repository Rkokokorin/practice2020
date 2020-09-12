package com.tuneit.itc.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Product {
    private String name;
    private String manufacturer;
    private String partNumber;
    private String secondPartNumber;
    private String typeName;
    private String id;
}
