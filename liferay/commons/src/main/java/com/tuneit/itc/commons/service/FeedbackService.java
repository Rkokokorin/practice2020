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

import com.tuneit.itc.commons.model.Feedback;


@ManagedBean
@ApplicationScoped
public class FeedbackService implements Serializable, BaseService<Long, Feedback> {
    private static final Log log = LogFactoryUtil.getLog(FeedbackService.class);
    @Getter
    @Setter
    @ManagedProperty("#{emf.em}")
    private EntityManager em;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return em;
    }

}
