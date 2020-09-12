package com.tuneit.itc.zuulproxy.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author Alexander Pashnin
 * Represents an external client of API
 */
@Entity
@Data
@Table(name = "external_user")
@EqualsAndHashCode(of = {"id"})
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    @NotBlank
    private String username;

    @Column
    @NotNull
    private Boolean enabled;

    @ManyToMany
    private Set<Role> roles;
}
