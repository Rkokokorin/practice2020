package com.tuneit.itc.commons.model;

import lombok.Data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.tuneit.itc.commons.service.BaseEntity;

@Entity
@Data
@Table(name = "itc_comparison_product", uniqueConstraints = {
    @UniqueConstraint(name = "user_comparison_product_once", columnNames = {"userId", "productCode"})
})
public class ComparisonProduct implements Serializable, BaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private long userId;

    @NotNull
    @NotBlank
    private String productCode;

}
