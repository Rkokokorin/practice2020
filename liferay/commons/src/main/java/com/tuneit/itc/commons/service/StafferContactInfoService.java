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

@ManagedBean
@ApplicationScoped
public class StafferContactInfoService implements Serializable, BaseService<Long, StafferContactInfo> {
    private static final Log log = LogFactoryUtil.getLog(OfficeContactInfoService.class);
    @Getter
    @Setter
    @ManagedProperty("#{emf.em}")
    private EntityManager em;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return em;
    }
}
