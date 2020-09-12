package com.tuneit.itc.zuulproxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomZuulRoute {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    @NotBlank
    private String uuid;
    @Column(unique = true)
    @NotBlank
    private String path;
    @Column
    @NotBlank
    private String outcomeUrl;
    @Column
    @NotNull
    private Boolean enabled;

    public CustomZuulRoute(@NotBlank String uuid, @NotBlank String path, @NotBlank String outcomeUrl,  @NotNull Boolean enabled) {
        this.uuid = uuid;
        this.outcomeUrl = outcomeUrl;
        this.enabled = enabled;
        this.path = path;
    }
}
