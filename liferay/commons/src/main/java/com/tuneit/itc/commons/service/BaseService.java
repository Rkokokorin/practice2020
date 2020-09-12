package com.tuneit.itc.commons.service;


import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static com.tuneit.itc.commons.ReflectionUtils.getGenericClass;

public interface BaseService<I extends Serializable, T extends BaseEntity<I>> {
    /**
     * Creates entity manager instance which can be used after closing with {@link EntityManager#close()}.
     *
     * <p>Entity manager returned by this method could be used after invoking {@link EntityManager#close()} method.
     * Proxy of somewhat else could be used.</p>
     *
     * @return reusable after closing entity manager instance.
     */
    EntityManager getOrCreateEntityManager();

    default List<T> findAll() {
        Class<T> actualType = getRepositoryClass();
        return doWithEntityManagerReturning(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> q = cb.createQuery(actualType);
            Root<T> from = q.from(actualType);
            q = q.select(from);
            return em
                .createQuery(q)
                .getResultStream()
                .collect(Collectors.toList());
        });
    }

    default Optional<T> find(I id) {
        Class<T> actualType = getRepositoryClass();
        return doWithEntityManagerReturning(em -> {
            T found = em.find(actualType, id);
            return Optional.ofNullable(found);
        });
    }

    default T save(T entity) {
        if (entity.getId() != null) {
            doInTransaction(em -> em.merge(entity));
        } else {
            doInTransaction(em -> em.persist(entity));
        }
        return entity;
    }

    default void delete(T entity) {
        doInTransaction(em -> em.remove(entity));
    }

    default List<T> execute(String jpqlQuery) {
        Class<T> actualType = getRepositoryClass();
        return doWithEntityManagerReturning(em -> {
            TypedQuery<T> query = em.createQuery(jpqlQuery, actualType);
            return query.getResultList();
        });
    }

    default List<T> execute(String jpqlQuery, Consumer<TypedQuery<T>> queryInitializer) {
        Class<T> actualType = getRepositoryClass();
        return doWithEntityManagerReturning(em -> {
            TypedQuery<T> query = em.createQuery(jpqlQuery, actualType);
            queryInitializer.accept(query);
            return query.getResultList();
        });
    }

    default void doInTransaction(Consumer<EntityManager> transactionBlock) {
        doInTransactionWithResult(manager -> {
            transactionBlock.accept(manager);
            return null;
        });
    }

    default <R> R doInTransactionWithResult(Function<EntityManager, R> transactionBlock) {
        return doWithEntityManagerReturning(em -> {
            EntityTransaction tr = null;
            R result = null;
            try {
                tr = em.getTransaction();
                if (!tr.isActive()) {
                    tr.begin();
                }
                result = transactionBlock.apply(em);
                if (tr.isActive()) {
                    tr.commit();
                }
                return result;
            } catch (Exception e) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                throw e;
            }
        });
    }

    default void doWithEntityManager(Consumer<EntityManager> work) {
        EntityManager em = getOrCreateEntityManager();
        work.accept(em);
        em.close();
    }

    default <R> R doWithEntityManagerReturning(Function<EntityManager, R> work) {
        EntityManager em = getOrCreateEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    default T attachToManager(T entity) {
        if (getOrCreateEntityManager().contains(entity)) {
            return entity;
        }
        return getOrCreateEntityManager().find(getRepositoryClass(), entity.getId());
    }

    default Class<T> getRepositoryClass() {
        Class<? extends BaseService> targetClass = this.getClass();
        int paramIndex = 1;
        return (Class<T>) getGenericClass(targetClass, paramIndex);
    }

}
