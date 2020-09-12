package com.tuneit.itc.commons.service;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;

import com.tuneit.itc.commons.model.ContractorSyntheticId;

@ManagedBean
@ApplicationScoped
public class ContractorSyntheticIdService implements BaseService<Long, ContractorSyntheticId>, Serializable {

    @Getter
    @Setter
    @ManagedProperty("#{emf.em}")
    private EntityManager entityManager;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return entityManager;
    }

}
