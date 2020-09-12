package com.tuneit.itc.commons.service.cart;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.tuneit.itc.commons.model.ITCProductReference;
import com.tuneit.itc.commons.model.cart.Cart;
import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.cart.CartPosition_;
import com.tuneit.itc.commons.model.cart.ProductOffer;
import com.tuneit.itc.commons.model.rest.SalesOffer;
import com.tuneit.itc.commons.service.BaseService;

@Data
@ManagedBean
@ApplicationScoped
public class CartPositionService implements BaseService<Long, CartPosition>, Serializable {
    @ManagedProperty("#{emf.em}")
    private EntityManager entityManager;

    @Override
    public EntityManager getOrCreateEntityManager() {
        return entityManager;
    }

    public CartPosition findForCart(Cart cart, String productCode) {
        return doInTransactionWithResult(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<CartPosition> cq = cb.createQuery(CartPosition.class);
            Root<CartPosition> root = cq.from(CartPosition.class);
            cq.select(root)
                .where(
                    cb.and(
                        cb.equal(root.get(CartPosition_.cart), cart),
                        cb.equal(root.get(CartPosition_.externalProductId), productCode)
                    )
                )
                .orderBy(cb.asc(root.get(CartPosition_.id)));
            TypedQuery<CartPosition> query = em.createQuery(cq);
            try {
                return query.getSingleResult();
            } catch (NoResultException noResult) {
                return null;
            }
        });
    }

    public List<CartPosition> findForCart(Cart cart) {
        if (cart == null) {
            return null;
        }
        return execute("SELECT cp FROM CartPosition cp WHERE cp.cart = :cart ORDER BY cp.id", q -> {
            q.setParameter("cart", cart);
        });

    }

    public List<CartPosition> findForCart(Cart cart, int limit) {
        if (cart == null) {
            return null;
        }
        return execute("SELECT cp FROM CartPosition cp WHERE cp.cart = :cart ORDER BY cp.count DESC", q -> {
            q.setParameter("cart", cart).setMaxResults(limit);
        });

    }

    public CartPosition addToCart(Cart cart, ITCProductReference product, long amount, String offerId, String queryId,
                                  Double price, String warehouseId, String currencyCode) {
        if (amount < 0) {
            amount = 0;
        }
        ProductOffer offer = null;
        if (offerId != null) {
            UUID offerUuid = UUID.fromString(offerId);
            UUID queryUuid = UUID.fromString(queryId);
            offer = new ProductOffer(queryUuid, offerUuid, currencyCode, warehouseId, price);
        }

        CartPosition position = this.findForCart(cart, product.getProductId());
        if (position != null) {
            position.setCount(position.getCount() + amount);
        } else {
            position = new CartPosition();
            position.setCart(cart);
            position.setCount(amount);
            position.setCreationDate(new Date());
            position.setExternalProductId(product.getProductId());
        }

        if (offer != null) {
            position.setSelectedOffer(offer);
        }

        return this.save(position);
    }

    public CartPosition addToCart(Cart cart, ITCProductReference product, long amount, SalesOffer offer,
                                  String warehouseId, String currencyCode) {
        String offerId = null;
        String queryId = null;
        Double price = null;
        if (offer != null) {
            offerId = offer.getSalesOfferId();
            queryId = offer.getRequestId();
            price = offer.getPersonalPrice() > 0 ? offer.getPersonalPrice() : offer.getPrice();
        }
        return this.addToCart(cart, product, amount, offerId, queryId, price, warehouseId, currencyCode);
    }

    public CartPosition updateOffer(CartPosition original, SalesOffer selectedOffer,
                                    String currencyCode, String warehouseId) {
        UUID queryId;
        UUID offerId;
        Double price;
        if (selectedOffer != null) {
            queryId = UUID.fromString(selectedOffer.getRequestId());
            offerId = UUID.fromString(selectedOffer.getSalesOfferId());
            price = selectedOffer.getPersonalPrice() > 0 ? selectedOffer.getPersonalPrice() : selectedOffer.getPrice();
        } else {
            price = null;
            queryId = null;
            offerId = null;
        }
        doInTransaction(em -> {
            em.createQuery("UPDATE CartPosition position " +
                "SET position.selectedOffer.currencyCode = :currencyCode, " +
                "position.selectedOffer.offerId = :offerId, " +
                "position.selectedOffer.queryId = :queryId, " +
                "position.selectedOffer.price = :price, " +
                "position.selectedOffer.warehouseId = :warehouseId " +
                "WHERE position.id = :pos")
                .setParameter("currencyCode", currencyCode)
                .setParameter("offerId", offerId)
                .setParameter("queryId", queryId)
                .setParameter("warehouseId", warehouseId)
                .setParameter("pos", original.getId())
                .setParameter("price", price)
                .executeUpdate();
        });
        return find(original.getId()).orElse(null);
    }
}
