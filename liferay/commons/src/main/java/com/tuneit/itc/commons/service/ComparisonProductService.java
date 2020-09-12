package com.tuneit.itc.commons.service;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.tuneit.itc.commons.model.ComparisonProduct;

@ManagedBean
@ApplicationScoped
public class ComparisonProductService implements Serializable, BaseService<Long, ComparisonProduct> {
    @Getter
    @Setter
    @ManagedProperty("#{emf.em}")
    private EntityManager entityManager;

    public ComparisonProduct findComparison(long userId, String productCode) {
        return doInTransactionWithResult(em -> {
            TypedQuery<ComparisonProduct> q = em.createQuery("SELECT fp FROM ComparisonProduct fp " +
                "WHERE fp.userId = :userId AND fp.productCode = :productCode", ComparisonProduct.class);
            q.setParameter("userId", userId).setParameter("productCode", productCode);
            try {
                return q.getSingleResult();
            } catch (NoResultException nre) {
                return null;
            }
        });
    }

    public List<ComparisonProduct> findAllForUser(long userId) {
        return execute("SELECT fp FROM ComparisonProduct fp WHERE fp.userId = :userId",
            q -> q.setParameter("userId", userId));
    }

    public List<ComparisonProduct> findAllForUserAndProductIdIn(long userId, List<String> codes) {
        return execute("SELECT fp FROM ComparisonProduct fp WHERE fp.userId = :userId and fp.productCode IN :codes",
            q -> {
                q.setParameter("userId", userId);
                q.setParameter("codes", codes);
            });
    }

    @Override
    public EntityManager getOrCreateEntityManager() {
        return entityManager;
    }
}
