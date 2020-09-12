package com.tuneit.itc.commons.service.cart;

import lombok.Data;

import java.math.BigInteger;
import java.util.Optional;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.commons.model.cart.PartnerCartSequence;
import com.tuneit.itc.commons.service.BaseService;

@Data
@ManagedBean
@ApplicationScoped
public class PartnerCartSequenceService implements BaseService<String, PartnerCartSequence> {

    private static final Log log = LogFactoryUtil.getLog(PartnerCartSequenceService.class);
    @ManagedProperty("#{emf.em}")
    private EntityManager entityManager;

    public long nextCartId(String partnerCode) {
        doInTransaction(em -> {
            Optional<PartnerCartSequence> foundSequence = find(partnerCode);
            if (foundSequence.isEmpty()) {
                PartnerCartSequence sequence = new PartnerCartSequence();
                sequence.setCartId(0);
                sequence.setPartnerCode(partnerCode);
                this.save(sequence);
            }
        });

        return doInTransactionWithResult(em -> {
            Query query = em.createNativeQuery("UPDATE itc_partner_cart_id_sequence SET cartid = cartid + 1 " +
                "WHERE partnercode = ?1 RETURNING cartid");
            query.setParameter(1, partnerCode);
            Object singleResult = query.getSingleResult();
            return ((BigInteger) singleResult).longValue();
        });
    }

    @Override
    public EntityManager getOrCreateEntityManager() {
        return entityManager;
    }
}
