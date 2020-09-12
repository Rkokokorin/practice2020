package com.tuneit.itc.zuulproxy.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

/**
 * @author Alexander Pashnin
 * Basic Auth user for inner services authorization
 */
@Entity
@Data
public class ApiUser {
    @Id
    @GeneratedValue
    private Long id;
    @Column
    @NotBlank
    private String username;
    @Column
    @NotBlank
    private String password;
}
