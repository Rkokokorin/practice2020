package com.tuneit.itc.commons.model.cart;

import lombok.Data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tuneit.itc.commons.service.BaseEntity;

@Data
@Entity
@Table(name = "itc_partner_cart_id_sequence")
public class PartnerCartSequence implements BaseEntity<String>, Serializable {
    @Id
    private String partnerCode;

    private long cartId;

    @Override
    public String getId() {
        return partnerCode;
    }

    @Override
    public void setId(String id) {
        this.partnerCode = id;
    }
}
