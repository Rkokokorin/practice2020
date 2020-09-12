package com.tuneit.itc.manufacturers.beans;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;

import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.rest.ManufacturerResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.manufacturers.model.Manufacturer;

@Data
@ManagedBean
@ViewScoped
public class ManufacturersListViewBean {
    private final String manIdParamName = ParamNameConstants.MANUFACTURER;
    Map<String, List<Manufacturer>> manufacturers = new HashMap<>();
    @ManagedProperty("#{requester.manufacturerService}")
    private Requester.ManufacturerService manufacturerService;
    private Log log = LogFactoryUtil.getLog(this.getClass());
    private String catalogueBaseUrl = "/catalogue";

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

            String firstLetter;
            List<Manufacturer> mnfList;
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

                firstLetter = mnf.getName().trim().substring(0, 1).toUpperCase();
                mnfList = manufacturers.get(firstLetter);

                if (mnfList == null) {
                    manufacturers.put(firstLetter, new ArrayList<>(Arrays.asList(mnf)));
                } else {
                    mnfList.add(mnf);
                }
            }

            try {
                LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
                catalogueBaseUrl = catalogueUrl.toString();
                catalogueBaseUrl = catalogueBaseUrl.substring(0, catalogueUrl.toString().indexOf('?'));
            } catch (PortalException e) {
                log.error("Can't get catalogue base url. " + e.getMessage());
                log.debug(e);
            }
        }
    }

    public List<Map.Entry<String, List<Manufacturer>>> getManufacturersAsList() {
        return new ArrayList<>(manufacturers.entrySet());
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
}
