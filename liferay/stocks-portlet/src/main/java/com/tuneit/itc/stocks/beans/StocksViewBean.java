package com.tuneit.itc.stocks.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.commons.RoleConstants;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.jsf.RoleChecker;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.rest.MarketPrice;
import com.tuneit.itc.commons.model.rest.NameCodePair;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.PortletPreferencesService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.OffersUtil;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;
import com.tuneit.itc.stocks.model.SpecialProduct;

@Data
@ManagedBean
@ViewScoped
public class StocksViewBean {
    public static final String PORTLET_CONFIGURATION_KEY = "STOCKS_PORTLET_CONFIG";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String selectedProductIdParamName = ParamNameConstants.PRODUCT;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    private List<StocksProduct> products;
    @ManagedProperty("#{liferay}")
    private Liferay liferay;
    private Random random = new Random(42);
    @ManagedProperty("#{currencyService.userCurrency}")
    private Currency preferredCurrency;
    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{requester.marketPricesService}")
    private Requester.MarketPricesService marketPricesService;
    @ManagedProperty("#{portletPreferencesService}")
    private PortletPreferencesService preferencesService;

    private ObjectMapper mapper = new ObjectMapper();

    private Mode mode;

    private Map<String, Double> pricesByProductId = new HashMap<>();
    private List<String> productIds;
    private List<SpecialProduct> specialProducts = new ArrayList<>();
    private Map<String, String> specialProductsDescriptions = new HashMap<>();

    @PostConstruct
    public void init() {
        loadConfig();
        mode = Mode.VIEW;
        productIds = specialProducts.stream().map(SpecialProduct::getProductCode).collect(Collectors.toList());
        updateProducts();
    }

    private void updateProducts() {
        List<ProductResponse> body = null;
        products = new ArrayList<>();
        productIds = specialProducts.stream().map(SpecialProduct::getProductCode).collect(Collectors.toList());
        try {
            body = productsService.getByIds(productIds).execute().body();
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage());
        }
        loadPrices();
        if (body == null || body.isEmpty()) {
            return;
        }
        this.products = body.stream()
            .map(pr -> pr.getHits().getHits())
            .filter(l -> !l.isEmpty())
            .map(pr -> pr.get(0).getSource())
            .map(phs -> new StocksProduct(phs, pricesByProductId.get(phs.getCode()),
                specialProductsDescriptions.get(phs.getCode())))
            .collect(Collectors.toList());
    }

    private void loadConfig() {
        var prefOpt = preferencesService.find(PORTLET_CONFIGURATION_KEY);
        if (prefOpt.isPresent()) {
            var prefs = prefOpt.get();
            try {
                StocksPortletConfig config = mapper.readValue(prefs.getValue(), StocksPortletConfig.class);
                specialProducts = config.getSpecialProducts();
                for (SpecialProduct specialProduct : specialProducts) {
                    specialProductsDescriptions.put(specialProduct.getProductCode(), specialProduct.getDescription());
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    /**
     * Save given configuration of the portlet to DB. Perform email validation.
     */
    public void savePreferences() {
        try {
            specialProducts.removeIf(sp -> sp.getProductCode() == null);
            var prefOpt = preferencesService.save(PORTLET_CONFIGURATION_KEY, new StocksPortletConfig(specialProducts));
        } catch (Exception e) {
            log.error(e);
            return;
        }
        loadConfig();
        updateProducts();
        setModeView();
    }

    public void resetPreferences() {
        mode = Mode.VIEW;
        loadConfig();
        updateProducts();
    }

    public void onAddNew() {
        // Add one new car to the table:
        SpecialProduct sp = new SpecialProduct();
        specialProducts.add(sp);
    }

    public boolean isAdmin() {
        return liferay.getThemeDisplay().getPermissionChecker().isOmniadmin()
            || RoleChecker.hasAnyGlobalRole(liferay.getUser().getUserId(), RoleConstants.WEB_CONTENT_EDITOR);
    }

    public String openProduct(ProductResponse.ProductHitSource productHitSource) {
        return String.format("/catalogue?faces-redirect=true&%s=%s",
            selectedProductIdParamName, productHitSource.getCode()
        );
    }

    public void updateCurrency() {
        preferredCurrency = currencyService.getUserCurrency();
        loadPrices();
        for (StocksProduct product : products) {
            product.setPrice(pricesByProductId.get(product.getProduct().getCode()));
        }
    }

    public String getProductCodeImageUrl(ProductResponse.ProductHitSource product) {
        return WindowsPathNormalizer.getProductImageUrl(product);
    }

    public String getRandomDiscount() {
        return String.valueOf(random.nextInt(40) + 10);
    }

    private void loadPrices() {
        try {
            Response<List<MarketPrice>> response = marketPricesService.getByIds(productIds, preferredCurrency,
                    OffersUtil.getCustomerId())
                .execute();
            if (!response.isSuccessful() || response.body() == null) {
                log.error("Can't load prices for currency {0}, ids: {1}. Response is: {2}",
                    preferredCurrency, productIds, response);
                return;
            }
            pricesByProductId = response
                .body()
                .stream()
                .collect(Collectors.toMap(MarketPrice::getNomenclatureCode, MarketPrice::getViewPrice));
        } catch (IOException exc) {
            log.error(exc);
            pricesByProductId = new HashMap<>();
        }
    }

    public boolean isModeEdit() {
        return mode == Mode.EDIT;
    }

    public boolean isModeView() {
        return mode == Mode.VIEW;
    }

    public void setModeEdit() {
        mode = Mode.EDIT;
    }

    public void setModeView() {
        mode = Mode.VIEW;
    }

    public void removeItem(SpecialProduct specialProduct) {
        specialProducts.remove(specialProduct);
    }

    private enum Mode {
        VIEW,
        EDIT
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StocksProduct implements Serializable {
        private ProductResponse.ProductHitSource product;
        private Double price;
        private String description;

        public NameCodePair getManufacturer() {
            return product.getManufacturer();
        }

        public NameCodePair getNomenclatureType() {
            return product.getNomenclatureType();
        }
    }
}
