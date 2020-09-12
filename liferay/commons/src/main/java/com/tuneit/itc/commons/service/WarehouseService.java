package com.tuneit.itc.commons.service;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.servlet.http.Cookie;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.model.User;

import com.tuneit.itc.commons.SessionConstants;
import com.tuneit.itc.commons.model.ExtendedUser;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.HttpUtil;

@Data
@ManagedBean
@ApplicationScoped
public class WarehouseService implements Serializable {

    private final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    @ManagedProperty("#{requester.warehousesService}")
    private Requester.WarehouseService warehouseService;


    @PostConstruct
    public void init() {
    }

    public List<Warehouse> loadWarehouses() {
        if (log.isTraceEnabled()) {
            Throwable throwable = new Throwable();
            log.trace("Stack trace: ", throwable);
        }
        try {
            Response<List<Warehouse>> execute = warehouseService.getAll().execute();
            List<Warehouse> body = execute.body();
            if (!execute.isSuccessful() || body == null) {
                log.error("Can not load warehouses from service, body {}", execute.errorBody());
                return new ArrayList<>();
            }
            return body.stream()
                .sorted(Comparator.comparing(Warehouse::getSortOrder).reversed().thenComparing(Warehouse::getName))
                .collect(Collectors.toList());
        } catch (IOException exc) {
            log.error(exc);
        }
        return new ArrayList<>();
    }

    public Warehouse getUserWarehouse() {
        return getUserWarehouse(LiferayPortletHelperUtil.getUser());
    }

    public Warehouse getUserWarehouse(List<Warehouse> warehouses) {
        return getUserWarehouse(warehouses, LiferayPortletHelperUtil.getUser());
    }

    public Warehouse getUserWarehouse(User user) {
        List<Warehouse> warehouses = loadWarehouses();
        return getUserWarehouse(warehouses, user);
    }

    public Warehouse getUserWarehouse(List<Warehouse> warehouses, User user) {
        String warehouseCode;
        if (user == null || user.isDefaultUser()) {
            log.debug("Get selected warehouse for null or default user");
            Cookie[] cookies = HttpUtil.getOriginalServletRequest().getCookies();
            warehouseCode = com.tuneit.itc.commons.util.HttpUtil.findCookie(cookies,
                    SessionConstants.SELECTED_WAREHOUSE_KEY)
                .map(Cookie::getValue)
                .orElse(null);
        } else {
            warehouseCode = new ExtendedUser(user).getWarehouseCode();
        }
        log.debug("Selected warehouse code {0}", warehouseCode);
        Warehouse selectedWarehouse = warehouses.stream()
            .filter(warehouse -> Objects.equals(warehouse.getCode(), warehouseCode))
            .findAny()
            .orElseGet(() -> getDefaultWarehouse(warehouses));
        log.debug("Selected warehouse {0}", selectedWarehouse);
        return selectedWarehouse;
    }

    public Warehouse getDefaultWarehouse(List<Warehouse> warehouses) {
        if (warehouses == null || warehouses.isEmpty()) {
            return null;
        }
        return warehouses.get(0);
    }

}
