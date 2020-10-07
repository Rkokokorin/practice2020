package com.tuneit.itc.commons.model;

import com.tuneit.itc.commons.service.BaseEntity;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@Entity
@Table(name = "itc_portal_office_contact_info")
public class OfficeContactInfo implements BaseEntity<Long>, Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50, unique = true)
    @NotBlank
    private String city;

    @Column(length = 50)
    @NotBlank
    private String name;

    @Column(length = 12)
    @Pattern(regexp = "^([+]?[\\d]{11})$")
    private String phone;

    @Column
    @Email
    private String email;

    @Column(length = 300)
    @NotBlank
    private String address;

    @Column(name = "weekdays_working_hours", length = 11)
    @NotBlank
    private String weekdaysWorkingHours;

    @Column(name = "weekends_working_hours", length = 11)
    @NotBlank
    private String weekendsWorkingHours;
}
