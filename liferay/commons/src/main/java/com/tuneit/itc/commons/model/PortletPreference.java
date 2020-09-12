package com.tuneit.itc.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import com.tuneit.itc.commons.service.BaseEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "itc_portal_portlet_preferences")
public class PortletPreference implements BaseEntity<String> {
    @Id
    private String key;
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String value;

    @Override
    public String getId() {
        return key;
    }

    @Override
    public void setId(String id) {
        this.key = id;
    }
}
