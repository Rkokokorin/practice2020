package com.tuneit.itc.commons.model;

import lombok.Data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.tuneit.itc.commons.service.BaseEntity;

@Data
@Entity
@Table(name = "itc_contractor_synthetic_id")
@SequenceGenerator(name = "contractor_id_sequence",
    sequenceName = "contractor_id_sequence", initialValue = 50, allocationSize = 1)
public class ContractorSyntheticId implements BaseEntity<Long>, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contractor_id_sequence")
    private Long id;
}
