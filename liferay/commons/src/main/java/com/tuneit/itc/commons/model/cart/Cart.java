package com.tuneit.itc.commons.model.cart;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.tuneit.itc.commons.service.BaseEntity;

@Data
@Entity
@Table(name = "itc_user_cart")
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"positions"})
public class Cart implements BaseEntity<Long>, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String contractorCode;

    private long contractorLocalCartId;

    private long ownerId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<CartPosition> positions;

    @Deprecated
    @Setter(AccessLevel.NONE)
    private boolean active;

    @Deprecated
    @Setter(AccessLevel.NONE)
    private boolean deleted;

    @Deprecated
    @Setter(AccessLevel.NONE)
    private boolean current;

    @Enumerated(EnumType.STRING)
    private CartState state;

    private String cartName;

    @Column(length = 2048)
    private String description;

    private Double savedTotalPrice;

    private String savedCurrencyId;

    private String savedWarehouseCode;

    @Transient
    public boolean isCurrent() {
        return state == CartState.CURRENT;
    }

    @Transient
    public boolean isActive() {
        return state == CartState.ACTIVE;
    }

    @Transient
    public boolean isDeleted() {
        return state == CartState.DELETED;
    }

    @Transient
    public String getReadableId() {
        return contractorCode + contractorLocalCartId;
    }

    @PrePersist
    public void prePersist() {
        if (this.creationDate == null) {
            this.creationDate = new Date();
        }
    }

    public enum CartState {
        ACTIVE,
        DELETED,
        CURRENT
    }

}
