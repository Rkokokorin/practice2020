package com.tuneit.itc.commons.service;

import lombok.Data;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.ExtendedUser;
import com.tuneit.itc.commons.model.rest.CurrencyResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.HttpUtil;

@Data
@ManagedBean
@ApplicationScoped
public class CurrencyService implements Serializable {

    private final Logger log = LoggerFactory.getLogger(CurrencyService.class);
    private final HashMap<String, String> currencySignsMap = new HashMap<>();
    @ManagedProperty("#{requester.currencyService}")
    private Requester.CurrencyService currencyService;

    @PostConstruct
    public void init() {
        currencySignsMap.put("USD", "$");
        currencySignsMap.put("EUR", "€");
        currencySignsMap.put("RUB", "₽");
    }

    public List<Currency> loadCurrencies() {
        if (log.isTraceEnabled()) {
            Throwable throwable = new Throwable();
            log.trace("Stack trace: ", throwable);
        }
        try {
            Response<CurrencyResponse> execute = currencyService.getAll().execute();
            CurrencyResponse body = execute.body();
            if (!execute.isSuccessful() || body == null) {
                ResponseBody responseBody = execute.errorBody();
                String message = null;
                if (responseBody != null) {
                    try {
                        message = responseBody.string();
                    } catch (IOException ioe) {
                        log.warn("Error while get error body {0}", ioe.getMessage());
                    }
                }
                log.error("Can not load currencies from service, body {}", message);
                return new ArrayList<>();
            }
            return body.getHits().getHits().stream()
                .map(ResponseBase.Hit::getSource)
                .map(currencyHit -> new Currency(currencyHit.getCode(), currencyHit.getName(),
                    currencySignsMap.get(currencyHit.getName())))
                .collect(Collectors.toList());
        } catch (IOException exc) {
            log.error("Error while fetch currencies: {0}", exc.getMessage());
        }
        return new ArrayList<>();
    }

    public Currency getUserCurrency() {
        return getUserCurrency(LiferayPortletHelperUtil.getUser());
    }

    public Currency getUserCurrency(User user) {
        List<Currency> currencies = loadCurrencies();
        return getUserCurrency(user, currencies);
    }

    public Currency getUserCurrency(List<Currency> currencies) {
        return getUserCurrency(LiferayPortletHelperUtil.getUser(), currencies);
    }

    public Currency getUserCurrency(User user, List<Currency> currencies) {
        String currencyCode;
        if (user == null || user.isDefaultUser()) {
            log.debug("Get selected currency for null or default user");
            currencyCode = com.tuneit.itc.commons.util.HttpUtil.findCookie(
                HttpUtil.getOriginalServletRequest().getCookies(),
                SessionConstants.SELECTED_CURRENCY_KEY)
                .map(Cookie::getValue)
                .orElse(null);

        } else {
            currencyCode = new ExtendedUser(user).getCurrencyCode();
        }
        log.debug("Selected currency code {0}", currencyCode);
        Currency selectedCurrency = currencies.stream()
            .filter(currency -> Objects.equals(currency.getCode(), currencyCode))
            .findAny()
            .orElseGet(() -> getDefaultCurrency(currencies));
        log.debug("Selected currency {0}", selectedCurrency);
        return selectedCurrency;
    }

    public Currency getDefaultCurrency(List<Currency> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            return null;
        }
        return currencies.get(0);
    }

    public String getSignForCurrency(String currency) {
        return currencySignsMap.getOrDefault(currency, currency);
    }

    public String getSignForCurrencyId(String currencyId) {
        List<Currency> currencies = loadCurrencies();
        Currency selectedCurrency = currencies.stream()
            .filter(currency -> Objects.equals(currency.getId(), currencyId))
            .findAny()
            .orElseGet(() -> getDefaultCurrency(currencies));
        return selectedCurrency.getSign();
    }

}
