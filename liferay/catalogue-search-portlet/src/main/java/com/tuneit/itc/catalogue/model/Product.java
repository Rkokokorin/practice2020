package com.tuneit.itc.catalogue.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Product {
    private String name;
    private String code;
    private String vendorCode;
    private String manufacturer;
    private String packaging;
    private String stock;
    private String price;
    private String imgUrl;
}
