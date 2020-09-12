package com.tuneit.itc.warehouse;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.Cookie;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import com.tuneit.itc.commons.SessionConstants;
import com.tuneit.itc.commons.jsf.ByIdConverter;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.ExtendedUser;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.WarehouseService;
import com.tuneit.itc.commons.util.HttpUtil;

@Data
@ManagedBean
@ViewScoped
public class WarehouseManager implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(WarehouseManager.class);
    private Warehouse selectedWarehouse;

    private List<Warehouse> warehouses;

    @ManagedProperty("#{warehouseService}")
    private WarehouseService warehouseService;

    private ByIdConverter<Warehouse> converter;


    @PostConstruct
    public void init() {
        this.warehouses = warehouseService.loadWarehouses();
        this.converter = new ByIdConverter<>(warehouses, Warehouse::getCode);
        String wh  = HttpUtil.getRequestParam(ParamNameConstants.WAREHOUSE);
        if (wh != null) {
            warehouses.stream().filter(warehouse -> warehouse.getCode().equals(wh)).findAny()
                    .ifPresent(this::setSelectedWarehouse);
            updateWarehouse();
            return;
        }
        selectedWarehouse = warehouseService.getUserWarehouse(warehouses);

    }

    public void updateWarehouse() {
        ThemeDisplay td = LiferayPortletHelperUtil.getThemeDisplay();
        User user = LiferayPortletHelperUtil.getUser();
        logger.debug("Set warehouse for user {0} to {1}", user.getUserId(), selectedWarehouse);
        if (td.isSignedIn()) {
            logger.debug("User is signed in - store warehouse in expando column");
            ExtendedUser extendedUser = new ExtendedUser(user);
            extendedUser.setWarehouseCode(selectedWarehouse.getCode());
            extendedUser.getUser().persist();
        } else {
            logger.debug("User is not signed in - store warehouse in session");
            Cookie cookie = new Cookie(SessionConstants.SELECTED_WAREHOUSE_KEY, selectedWarehouse.getCode());
            cookie.setPath("/");
            cookie.setMaxAge(Integer.MAX_VALUE);
            cookie.setHttpOnly(true);
            HttpUtil.getPortletServletResponse()
                .addCookie(cookie);
        }
    }

}
