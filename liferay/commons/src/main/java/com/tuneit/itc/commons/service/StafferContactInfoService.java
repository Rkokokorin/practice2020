package com.tuneit.itc.commons.service;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.tuneit.itc.commons.model.StafferContactInfo;
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
public class StafferContactInfoService implements Serializable, BaseService<Long, StafferContactInfo> {
    private static final Log log = LogFactoryUtil.getLog(StafferContactInfoService.class);
    @Getter
    @Setter
    @ManagedProperty("#{emf.em}")
    private EntityManager em;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return em;
    }

    public List<StafferContactInfo> getAllOfficeStaffers(Long officeId) {
        return em.createQuery("SELECT t FROM StafferContactInfo t WHERE t.officeContactInfo.id = :value1", StafferContactInfo.class)
                .setParameter("value1", officeId).getResultList();
    }
}
