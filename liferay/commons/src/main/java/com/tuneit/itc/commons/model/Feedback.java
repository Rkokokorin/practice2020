package com.tuneit.itc.commons.model;

import lombok.Data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.tuneit.itc.commons.service.BaseEntity;

@Data
@Entity
@Table(name = "itc_portal_feedback")
public class Feedback implements BaseEntity<Long>, Serializable {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    @Column(length = 2000)
    @NotBlank
    private String text;
    @Column
    @NotBlank
    private String email;
    @Column
    @NotBlank
    private String name;
}

