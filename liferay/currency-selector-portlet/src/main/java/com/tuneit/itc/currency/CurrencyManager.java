package com.tuneit.itc.currency;

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
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.ExtendedUser;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.util.HttpUtil;

@Data
@ManagedBean
@ViewScoped
public class CurrencyManager implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(CurrencyManager.class);
    private Currency selectedCurrency;

    private List<Currency> currencies;

    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;

    private ByIdConverter<Currency> converter;

    @PostConstruct
    public void init() {
        this.currencies = currencyService.loadCurrencies();
        this.converter = new ByIdConverter<>(currencies, Currency::getCode);
        selectedCurrency = currencyService.getUserCurrency(currencies);
    }

    public void updateCurrency() {
        ThemeDisplay td = LiferayPortletHelperUtil.getThemeDisplay();
        User user = LiferayPortletHelperUtil.getUser();
        logger.debug("Set currency for user {0} to {1}", user.getUserId(), selectedCurrency);
        if (td.isSignedIn()) {
            logger.debug("User is signed in - store currency in expando column");
            ExtendedUser extendedUser = new ExtendedUser(user);
            extendedUser.setCurrencyCode(selectedCurrency.getCode());
            extendedUser.getUser().persist();
        } else {
            logger.debug("User is not signed in - store currency in session");
            Cookie cookie = new Cookie(SessionConstants.SELECTED_CURRENCY_KEY, selectedCurrency.getCode());
            cookie.setPath("/");
            cookie.setMaxAge(Integer.MAX_VALUE);
            cookie.setHttpOnly(true);
            HttpUtil.getPortletServletResponse().addCookie(cookie);
        }
    }

}
