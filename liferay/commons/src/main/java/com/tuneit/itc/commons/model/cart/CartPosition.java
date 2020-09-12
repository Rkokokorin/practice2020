package com.tuneit.itc.commons.model.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.tuneit.itc.commons.model.ITCProductReference;
import com.tuneit.itc.commons.service.BaseEntity;

@Data
@Entity
@Table(name = "itc_user_cart_position")
@EqualsAndHashCode(of = {"id", "count"})
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CartPosition implements BaseEntity<Long>, Serializable, ITCProductReference {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String externalProductId;

    private String productDescription;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    private long count;

    @ManyToOne(optional = false)
    private Cart cart;

    @Embedded
    private ProductOffer selectedOffer;

    @Transient
    public double getTotalPrice() {
        if (getSelectedOffer() == null) {
            return 0.0;
        }
        if (getSelectedOffer().getPrice() == null) {
            return 0.0;
        }
        return count * getSelectedOffer().getPrice();
    }

    @Transient
    public String getCurrencyCode() {
        if (getSelectedOffer() == null) {
            return null;
        }
        return getSelectedOffer().getCurrencyCode();
    }

    @Transient
    public String getWarehouseId() {
        if (getSelectedOffer() == null) {
            return null;
        }
        return getSelectedOffer().getWarehouseId();
    }

    @PrePersist
    private void prePersist() {
        if (this.creationDate == null) {
            this.creationDate = new Date();
        }
    }

    @Transient
    @Override
    public String getProductId() {
        return getExternalProductId();
    }
}
