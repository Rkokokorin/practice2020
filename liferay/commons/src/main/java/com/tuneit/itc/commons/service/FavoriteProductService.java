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

import com.tuneit.itc.commons.model.FavoriteProduct;

@ManagedBean
@ApplicationScoped
public class FavoriteProductService implements Serializable, BaseService<Long, FavoriteProduct> {
    @Getter
    @Setter
    @ManagedProperty("#{emf.em}")
    private EntityManager entityManager;

    public FavoriteProduct findFavorite(long userId, String productCode) {
        return doInTransactionWithResult(em -> {
            TypedQuery<FavoriteProduct> q = em.createQuery("SELECT fp FROM FavoriteProduct fp " +
                "WHERE fp.userId = :userId AND fp.productCode = :productCode", FavoriteProduct.class);
            q.setParameter("userId", userId).setParameter("productCode", productCode);
            try {
                return q.getSingleResult();
            } catch (NoResultException nre) {
                return null;
            }
        });
    }

    public void remove(long userId, String productCode) {
        doInTransaction(em -> {
            em.createQuery("DELETE FROM FavoriteProduct fv WHERE fv.userId = :userId AND fv.productCode = :productCode")
                .setParameter("userId", userId)
                .setParameter("productCode", productCode)
                .executeUpdate();
        });
    }

    public List<FavoriteProduct> findAllForUser(long userId) {
        return execute("SELECT fp FROM FavoriteProduct fp WHERE fp.userId = :userId",
            q -> q.setParameter("userId", userId));
    }

    public List<FavoriteProduct> findForUser(long userId, int pageNumber, int pageSize) {
        return execute("SELECT fp FROM FavoriteProduct fp WHERE fp.userId = :userId",
            q -> {
                q.setParameter("userId", userId);
                q.setFirstResult((pageNumber - 1) * pageSize);
                q.setMaxResults(pageSize);
            });
    }

    public long countForUser(long userId) {
        return doInTransactionWithResult(em -> {
            TypedQuery<Long> q = em.createQuery("SELECT count(fp.id) FROM FavoriteProduct fp " +
                "WHERE fp.userId = :userId", Long.class);
            q.setParameter("userId", userId);
            try {
                return q.getSingleResult();
            } catch (NoResultException nre) {
                return null;
            }
        });
    }


    public List<FavoriteProduct> findAllForUserAndProductIdIn(long userId, List<String> codes) {
        return execute("SELECT fp FROM FavoriteProduct fp WHERE fp.userId = :userId and fp.productCode IN :codes",
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
