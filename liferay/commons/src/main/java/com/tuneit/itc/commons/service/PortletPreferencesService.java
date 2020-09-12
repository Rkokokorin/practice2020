package com.tuneit.itc.commons.service;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.commons.model.JsonConvertible;
import com.tuneit.itc.commons.model.PortletPreference;

@ManagedBean
@ApplicationScoped
public class PortletPreferencesService implements Serializable, BaseService<String, PortletPreference> {
    private static final Log log = LogFactoryUtil.getLog(PortletPreferencesService.class);
    @ManagedProperty("#{emf.em}")
    @Getter
    @Setter
    private EntityManager em;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return em;
    }

    public PortletPreference save(String key, JsonConvertible value) throws Exception {
        return save(new PortletPreference(key, value.toJsonString()));
    }
}
