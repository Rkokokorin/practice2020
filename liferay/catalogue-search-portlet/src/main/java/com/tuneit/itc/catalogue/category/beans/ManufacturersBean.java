package com.tuneit.itc.catalogue.category.beans;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.catalogue.model.Manufacturer;
import com.tuneit.itc.commons.model.rest.ManufacturerResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.rest.Requester;

@Data
@ManagedBean
@ViewScoped
public class ManufacturersBean {
    Map<String, Manufacturer> manufacturersMap = new HashMap<>();
    List<Manufacturer> manufacturers = new ArrayList<>();
    @ManagedProperty("#{requester.manufacturerService}")
    private Requester.ManufacturerService manufacturerService;
    private Log log = LogFactoryUtil.getLog(this.getClass());

    @PostConstruct
    public void init() {
        long size = 5000;
        long from = 0;

        var manufacturerHitSourceHits = requestManufacturers(size, from);
        if (manufacturerHitSourceHits != null) {
            var hits = manufacturerHitSourceHits.getHits();
            long total = manufacturerHitSourceHits.getTotal().getValue();

            if (total > size) {
                from = size;
                size = total - size;
                var allManufacturers = requestManufacturers(size, from);
                if (allManufacturers != null) {
                    hits.addAll(allManufacturers.getHits());
                }
            }

            for (ResponseBase.Hit<ManufacturerResponse.ManufacturerHitSource> h : hits) {
                if (h.getSource().getSortingOrder() < 0) {
                    continue;
                }
                Manufacturer mnf = new Manufacturer(
                    h.getSource().getCode(),
                    h.getSource().getName(),
                    h.getSource().getFullName(),
                    h.getSource().getNameEng(),
                    h.getSource().getFullNameEng(),
                    h.getSource().getAssociatedProducts()
                );
                manufacturers.add(mnf);
                manufacturers.sort(Comparator.comparing(Manufacturer::getName));
                manufacturersMap.put(mnf.getId(), mnf);
            }
        }
    }

    public List<Manufacturer> searchManufacturers(String query) {
        List<Manufacturer> result = new ArrayList<>();

        long size = 5000;
        long from = 0;

        var manufacturerHitSourceHits = requestManufacturers(size, from, query);
        if (manufacturerHitSourceHits != null) {
            var hits = manufacturerHitSourceHits.getHits();
            long total = manufacturerHitSourceHits.getTotal().getValue();

            if (total > size) {
                from = size;
                size = total - size;
                var manufacturersFound = requestManufacturers(size, from, query);
                if (manufacturersFound != null) {
                    hits.addAll(manufacturersFound.getHits());
                }
            }

            for (ResponseBase.Hit<ManufacturerResponse.ManufacturerHitSource> h : hits) {
                if (h.getSource().getSortingOrder() < 0) {
                    continue;
                }
                Manufacturer mnf = new Manufacturer(
                    h.getSource().getCode(),
                    h.getSource().getName(),
                    h.getSource().getFullName(),
                    h.getSource().getNameEng(),
                    h.getSource().getFullNameEng(),
                    h.getSource().getAssociatedProducts()
                );
                result.add(mnf);
                result.sort(Comparator.comparing(Manufacturer::getName));
            }
        }

        return result;
    }

    private ResponseBase.Hits<ManufacturerResponse.ManufacturerHitSource> requestManufacturers(long size, long from) {
        Response<ManufacturerResponse> response;
        try {
            response = manufacturerService.page(from, size).execute();
            if (response.code() != 200) {
                log.error("Can't fetch manufacturers. Response code is " + response.code()
                    + ". Message " + response.message());
                return null;
            }
            ManufacturerResponse body = response.body();
            if (body == null) {
                log.error("Can't fetch manufacturers. Response body is null");
                return null;
            }
            return body.getHits();
        } catch (IOException e) {
            log.error("Can't fetch manufacturers. " + e.getMessage());
            log.debug(e);
        }
        return null;
    }

    private ResponseBase.Hits<ManufacturerResponse.ManufacturerHitSource> requestManufacturers(long size,
                                                                                               long from,
                                                                                               String query) {
        Response<ManufacturerResponse> response;
        try {
            response = manufacturerService.query(query, from, size).execute();
            if (response.code() != 200) {
                log.error("Can't fetch manufacturers. Response code is " + response.code() +
                    ". Message " + response.message());
                return null;
            }
            ManufacturerResponse body = response.body();
            if (body == null) {
                log.error("Can't fetch manufacturers. Response body is null");
                return null;
            }
            return body.getHits();
        } catch (IOException e) {
            log.error("Can't fetch manufacturers. " + e.getMessage());
            log.debug(e);
        }
        return null;
    }
}
