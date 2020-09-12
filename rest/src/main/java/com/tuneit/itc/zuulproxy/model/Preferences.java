package com.tuneit.itc.zuulproxy.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class Preferences {
    @Id
    @GeneratedValue
    private Long id;
    @Column
    @NotBlank
    private String authHeader;
    @Column
    @NotNull
    private Boolean authEnabled;
}
