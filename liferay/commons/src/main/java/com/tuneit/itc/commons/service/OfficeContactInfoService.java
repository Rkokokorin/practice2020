package com.tuneit.itc.commons.service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.tuneit.itc.commons.model.OfficeContactInfo;
import lombok.Getter;
import lombok.Setter;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

@ManagedBean
@ApplicationScoped
public class OfficeContactInfoService implements Serializable, BaseService<Long, OfficeContactInfo> {
    private static final Log log = LogFactoryUtil.getLog(OfficeContactInfoService.class);
    @Getter
    @Setter
    @ManagedProperty("#{emf.em}")
    private EntityManager em;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return em;
    }

    public OfficeContactInfo findByCity(String city) {
       return em.createQuery("SELECT t FROM OfficeContactInfo t where t.city = :value1", OfficeContactInfo.class)
                .setParameter("value1", city).getSingleResult();
    }

    public List<String> getNamesOfAllCities() {
        return em.createQuery("SELECT t.city FROM OfficeContactInfo t", String.class).getResultList();
    }
}
