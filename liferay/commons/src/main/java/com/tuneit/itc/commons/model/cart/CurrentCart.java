package com.tuneit.itc.commons.model.cart;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.tuneit.itc.commons.service.BaseEntity;

@Entity
@Table(name = "itc_current_carts")
@Data
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
public class CurrentCart implements Serializable, BaseEntity<Long> {
    @Id
    @GeneratedValue
    private Long id;

    private long userId;

    @ManyToOne
    private Cart associatedCart;

    public CurrentCart(long userId, Cart associatedCart) {
        this.userId = userId;
        this.associatedCart = associatedCart;
    }
}
