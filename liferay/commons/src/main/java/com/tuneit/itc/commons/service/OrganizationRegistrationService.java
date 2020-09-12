package com.tuneit.itc.commons.service;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.commons.model.OrganizationRegistrationClaim;

@ManagedBean
@ApplicationScoped
public class OrganizationRegistrationService implements Serializable, BaseService<Long, OrganizationRegistrationClaim> {
    private static final Log log = LogFactoryUtil.getLog(OrganizationRegistrationService.class);
    @ManagedProperty("#{emf.em}")
    @Getter
    @Setter
    private EntityManager em;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return em;
    }

    public List<OrganizationRegistrationClaim> findActive(boolean active) {
        return execute("SELECT o FROM OrganizationRegistrationClaim o WHERE o.active = :active", query -> {
            query.setParameter("active", active);
        });
    }

}
