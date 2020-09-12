package com.tuneit.itc.commons.model.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Embeddable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ProductOffer implements Serializable {

    private UUID queryId;

    private UUID offerId;

    private String currencyCode;

    private String warehouseId;

    private Double price;

}
