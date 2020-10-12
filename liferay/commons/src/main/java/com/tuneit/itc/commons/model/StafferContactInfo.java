package com.tuneit.itc.commons.model;

import com.tuneit.itc.commons.service.BaseEntity;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@Entity
@Table(name = "itc_portal_staffer_contact_info")
public class StafferContactInfo implements BaseEntity<Long>, Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private OfficeContactInfo officeContactInfo;

    @Column(length = 50)
    @NotBlank
    private String department;

    @Column(length = 50)
    @NotBlank
    private String name;

    @Column(length = 50)
    @NotBlank
    private String position;

    @Column(length = 12)
    @NotBlank
    @Pattern(regexp = "^([+]?[\\d]{11})$")
    private String phone;

    @Column
    @NotBlank
    @Email
    private String email;
}

