package com.tuneit.itc.commons.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.tuneit.itc.commons.service.BaseEntity;

@Data
@Entity
@Table(name = "itc_org_registration_claim")
public class OrganizationRegistrationClaim implements Serializable, BaseEntity<Long> {
    @Id
    @GeneratedValue
    private Long id;

    private boolean juridicalPerson;
    @Column(length = 2048)
    private String companyFullName;
    @Column(length = 1024)
    private String companyShortName;
    private String tin;
    private String msrn;
    private String adminName;
    private String adminSurname;
    private String adminPatronymicName;
    @Column(length = 1024)
    private String contacts;
    private String phone;
    private String email;

    private boolean active = true;
    private Long assignedUser;

    private Long associatedOrganization;

    public String getFullName() {
        return Stream.of(adminSurname, adminName, adminPatronymicName)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(" "));
    }

}
