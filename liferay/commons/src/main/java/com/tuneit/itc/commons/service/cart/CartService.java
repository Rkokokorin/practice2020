package com.tuneit.itc.commons.service.cart;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.cart.Cart;
import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.cart.CartPosition_;
import com.tuneit.itc.commons.model.cart.Cart_;
import com.tuneit.itc.commons.model.cart.CurrentCart;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.BaseService;

@Data
@ManagedBean
@ApplicationScoped
public class CartService implements BaseService<Long, Cart>, Serializable {

    @ManagedProperty("#{emf.em}")
    private EntityManager entityManager;

    @ManagedProperty("#{partnerCartSequenceService}")
    private PartnerCartSequenceService sequenceService;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private CurrentCartService currentCartService = new CurrentCartService();

    @Override
    public EntityManager getOrCreateEntityManager() {
        return entityManager;
    }

    public Cart createInitialCart(ExtendedOrganization contractor, long userId) {
        return createInitialCart(contractor, userId, null);
    }

    public Cart createInitialCart(ExtendedOrganization contractor, long userId, String cartName) {
        String contractorCode = contractor.getSyntheticCode();
        long newId = sequenceService.nextCartId(contractorCode);
        Cart cart = new Cart();
        cart.setCartName(cartName);
        cart.setContractorCode(contractorCode);
        cart.setState(Cart.CartState.CURRENT);
        cart.setCreationDate(new Date());
        cart.setOwnerId(userId);
        cart.setContractorLocalCartId(newId);
        var savedCart = this.save(cart);
        CurrentCart currentCart = new CurrentCart(userId, savedCart);
        currentCartService.save(currentCart);
        return savedCart;
    }

    public Cart findCurrentForUser(long userId) {
        List<Cart> results = this.execute("SELECT c FROM CurrentCart cart JOIN cart.associatedCart c " +
            "WHERE c.state = :state AND cart.userId = :userId",
            q -> {
                q.setParameter("userId", userId);
                q.setParameter("state", Cart.CartState.CURRENT);
            }
        );
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public Cart removeFromCurrent(Cart cart, Cart.CartState newState, double totalPrice, Currency savedCurrency,
                                  Warehouse savedWarehouse) {
        if (newState == Cart.CartState.CURRENT) {
            throw new IllegalStateException("new state can not be " + newState);
        }
        return doInTransactionWithResult(em -> {
            var attached = entityManager.merge(cart);
            this.currentCartService.deleteCurrentForUser(attached, attached.getOwnerId());
            attached.setState(newState);
            attached.setSavedTotalPrice(totalPrice);
            attached.setSavedCurrencyId(Optional.ofNullable(savedCurrency).map(Currency::getId).orElse(null));
            attached.setSavedWarehouseCode(Optional.ofNullable(savedWarehouse).map(Warehouse::getCode).orElse(null));
            em.persist(attached);
            return attached;
        });
    }


    public List<CartWithPositionsCount> findCartsForUser(long userId, boolean active) {
        return this.doInTransactionWithResult(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
            Root<Cart> root = q.from(Cart.class);
            ListJoin<Cart, CartPosition> pos = root.join(Cart_.positions);
            q.multiselect(root, cb.count(pos), cb.sum(pos.get(CartPosition_.count)));
            Predicate filter;
            if (active) {
                filter = cb.equal(root.get(Cart_.state), Cart.CartState.ACTIVE);
            } else {
                filter = cb.equal(root.get(Cart_.state), Cart.CartState.DELETED);
            }
            q.where(
                cb.and(
                    cb.equal(root.get(Cart_.ownerId), userId),
                    filter
                )
            );
            return getCartWithPositionsCounts(root, em, q);
        });
    }

    public List<CartWithPositionsCount> findForUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return this.doInTransactionWithResult(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
            Root<Cart> root = q.from(Cart.class);
            ListJoin<Cart, CartPosition> pos = root.join(Cart_.positions);
            q.multiselect(root, cb.count(pos), cb.sum(pos.get(CartPosition_.count)));
            q.where(
                cb.and(
                    root.get(Cart_.ownerId).in(userIds),
                    cb.equal(root.get(Cart_.state), Cart.CartState.ACTIVE)
                )
            );
            return getCartWithPositionsCounts(root, em, q);
        });
    }

    private List<CartWithPositionsCount> getCartWithPositionsCounts(Root<Cart> root, EntityManager em,
                                                                    CriteriaQuery<Object[]> q) {
        q.groupBy(root.get(Cart_.id));
        TypedQuery<Object[]> query = em.createQuery(q);
        List<Object[]> resultList = query.getResultList();
        return resultList.stream()
            .map(os -> {
                Cart cart = (Cart) os[0];
                long positions = ((Long) os[1]);
                long total = ((Long) os[2]);
                return new CartWithPositionsCount(cart, positions, total);
            })
            .collect(Collectors.toList());
    }

    public Cart cloneCart(ExtendedOrganization contractor, long userId, List<CartPosition> positions, String cartName) {
        Cart initialCart = createInitialCart(contractor, userId, cartName);
        List<CartPosition> newPositions = positions.stream()
            .map(CartPosition::toBuilder)
            .map(builder -> builder.id(null)
                .cart(initialCart)
                .creationDate(initialCart.getCreationDate())
            )
            .map(CartPosition.CartPositionBuilder::build)
            .collect(Collectors.toList());
        doInTransaction(em -> {
            for (CartPosition newPosition : newPositions) {
                em.persist(newPosition);
            }
        });
        return find(initialCart.getId()).orElseThrow();
    }

    public Cart find(String contractorCode, long contractorLocalCartId) {
        contractorCode = contractorCode.toLowerCase();
        EntityManager em = getOrCreateEntityManager();
        TypedQuery<Cart> query = em.createQuery("SELECT c FROM Cart c " +
            "WHERE lower(c.contractorCode) = :contractorCode " +
            "AND c.contractorLocalCartId = :localId AND c.state = :state", Cart.class);
        try {
            return query.setParameter("state", Cart.CartState.CURRENT)
                .setParameter("contractorCode", contractorCode)
                .setParameter("localId", contractorLocalCartId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void assignAsCurrent(Cart cart, long userId) {
        if (!cart.isCurrent()) {
            throw new IllegalArgumentException("Can not assign cart");
        }
        CurrentCart currentCart = new CurrentCart(userId, cart);
        currentCartService.save(currentCart);
    }

    public void unassignCart(Cart cart, long userId) {
        doInTransaction(em -> {
            currentCartService.deleteCurrentForUser(cart, userId);
        });
    }

    @Value
    public static class CartWithPositionsCount {
        private Cart cart;
        private long positionsCount;
        private long totalPositions;
    }

    private class CurrentCartService implements BaseService<Long, CurrentCart> {

        @Override
        public EntityManager getOrCreateEntityManager() {
            return CartService.this.entityManager;
        }

        private void deleteCurrentForUser(Cart cart, long userId) {
            Query query = entityManager.createQuery("DELETE FROM CurrentCart c " +
                "WHERE c.associatedCart = :cart AND c.userId = :userId");
            query.setParameter("cart", cart);
            query.setParameter("userId", userId);
            query.executeUpdate();
        }
    }
}
