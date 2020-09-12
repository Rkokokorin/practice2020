package com.tuneit.itc.favorites;


import lombok.Data;
import lombok.SneakyThrows;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.SortOrder;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.util.StringUtil;

import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.ComparisonProduct;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.FavoriteProduct;
import com.tuneit.itc.commons.model.rest.MarketPrice;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.service.ComparisonProductService;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.FavoriteProductService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.FunctionUtils;
import com.tuneit.itc.commons.util.HttpUtil;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.commons.util.OffersUtil;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;
import com.tuneit.itc.favorites.utils.ProductLazyDataModel;
import com.tuneit.itc.favorites.utils.ProductRow;

@ManagedBean
@ViewScoped
@Data
public class FavoritesList {

    @ManagedProperty("#{favoriteProductService}")
    private FavoriteProductService favoriteProductService;
    @ManagedProperty("#{comparisonProductService}")
    private ComparisonProductService comparisonProductService;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{requester.marketPricesService}")
    private Requester.MarketPricesService marketPricesService;
    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{cartBean}")
    private CartBean cartBean;

    private ProductLazyDataModel productLazyDataModel;
    private Currency preferredCurrency;
    private Map<String, Double> pricesByProducts;

    private String plpageParamName = ParamNameConstants.PLPAGE;
    private String plrowsParamName = ParamNameConstants.PLROWS;

    private Integer currentTablePage = 0;
    private Integer currentTableRows = 5;


    private boolean signedIn;
    private long userId;
    private String heartSvg;
    private String compareSvg;


    private Logger log = LoggerFactory.getLogger(FavoritesList.class);

    public void pageEventListener(PageEvent pe) {
        currentTablePage = pe.getPage();
        currentTableRows = ((DataTable) pe.getComponent()).getRows();
    }

    @PostConstruct
    public void init() {
        this.signedIn = LiferayPortletHelperUtil.getThemeDisplay().isSignedIn();
        this.userId = LiferayPortletHelperUtil.getUserId();
        if (!isSignedIn()) {
            return;
        }
        productLazyDataModel = new ProductLazyDataModel(productsService, comparisonProductService,
            favoriteProductService, userId);
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            var heart = ec.getResourceAsStream("/WEB-INF/resources/images/heart.svg");
            var compare = ec.getResourceAsStream("/WEB-INF/resources/images/compare.svg");
            heartSvg = StringUtil.read(heart);
            compareSvg = StringUtil.read(compare);
        } catch (Exception exc) {
            log.error("Error while svg initialization!", exc);
        }
        pricesByProducts = new HashMap<>();
        preferredCurrency = currencyService.getUserCurrency();

        DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
            .findComponent("favoriteForm:listDataTable");
        if (d != null) {
            int rows = currentTableRows;
            String plrows = HttpUtil.getRequestParam("plrows");
            if (null != plrows) {
                try {
                    rows = Integer.parseInt(plrows);
                } catch (NumberFormatException e) {
                    log.warn("Incorrect rows amount", e);
                }
            }
            d.setRows(rows);
            int page = 0;
            String plpage = HttpUtil.getRequestParam("plpage");
            if (null != plpage) {
                try {
                    page = Integer.parseInt(plpage);
                } catch (NumberFormatException e) {
                    log.warn("Incorrect page number", e);
                }
            }
            page = Math.max(page, 0);
            int first = page * d.getRowsToRender();
            d.setFirst(first);
            productLazyDataModel.load(d.getFirst(), d.getRows(),
                d.getSortField(),
                SortOrder.valueOf(d.getSortOrder().toUpperCase()),
                d.getFilters());
        }
        loadPrices();
    }


    public void removeFavorite(ProductResponse.ProductHitSource productRow) {
        favoriteProductService.remove(LiferayPortletHelperUtil.getUserId(), productRow.getCode());
        DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
            .findComponent("favoriteForm:listDataTable");
        d.loadLazyData();
    }

    public String getProductPictureUrl(ProductResponse.ProductHitSource product) {
        return WindowsPathNormalizer.getProductImageUrl(product);
    }


    @SneakyThrows
    public String productViewUrl(String productCode) {
        LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
        catalogueUrl.setParameter(ParamNameConstants.PRODUCT, productCode);
        return catalogueUrl.toString();
    }


    public void loadPrices() {
        List<ProductResponse.ProductHitSource> currentPage = productLazyDataModel.getCurrentPage()
            .stream()
            .map(ProductRow::getProduct)
            .collect(Collectors.toList());
        if (currentPage != null) {
            List<String> currentPageProductIds = currentPage.stream()
                .map(ProductResponse.ProductHitSource::getCode)
                .filter(productId -> !pricesByProducts.containsKey(productId))
                .distinct()
                .collect(Collectors.toList());
            log.debug("Current page products: {0}", currentPageProductIds);
            if (currentPageProductIds.isEmpty()) {
                return;
            }
            Response<List<MarketPrice>> prices;
            try {
                prices = marketPricesService.getByIds(currentPageProductIds, preferredCurrency,
                        OffersUtil.getCustomerId()).execute();
            } catch (IOException exc) {
                log.warn("Unexpected error", exc);
                return;
            }
            List<MarketPrice> body = prices.body();
            if (body == null) {
                log.warn("Body for prices was null");
                return;
            }
            Map<String, Double> requestedPrices = body.stream()
                .collect(Collectors.toMap(MarketPrice::getNomenclatureCode, MarketPrice::getViewPrice,
                    FunctionUtils::fst));
            log.debug("Newly loaded prices: {0}", requestedPrices);
            pricesByProducts.putAll(requestedPrices);
        }
    }

    public void updateCurrency() {
        Currency userCurrency = currencyService.getUserCurrency();
        if (!userCurrency.equals(preferredCurrency)) {
            preferredCurrency = userCurrency;
            pricesByProducts = new HashMap<>();
            loadPrices();
        }

    }

    public void toggleComparison(ProductRow productRow) {
        if (!isSignedIn()) {
            return;
        }

        ComparisonProduct comparisonProduct = productRow.getComparison();
        if (comparisonProduct == null) {
            ComparisonProduct cp = new ComparisonProduct();
            cp.setUserId(userId);
            cp.setProductCode(productRow.getProduct().getCode());
            productRow.setComparison(comparisonProductService.save(cp));
        } else {
            productRow.setComparison(comparisonProductService.attachToManager(productRow.getComparison()));
            comparisonProductService.delete(productRow.getComparison());
            productRow.setComparison(null);
        }
    }


    public void toggleFavorite(ProductRow productRow) {
        FavoriteProduct favoriteProduct = productRow.getFavorite();
        if (favoriteProduct == null) {
            FavoriteProduct cp = new FavoriteProduct();
            cp.setUserId(userId);
            cp.setProductCode(productRow.getProduct().getCode());
            productRow.setFavorite(favoriteProductService.save(cp));
        } else {
            productRow.setFavorite(favoriteProductService.attachToManager(productRow.getFavorite()));
            favoriteProductService.delete(productRow.getFavorite());
            productRow.setFavorite(null);
        }
    }

    public void addToCart(ProductRow productRow) {
        if (!isSignedIn()) {
            return;
        }
        if (!cartBean.isContractorEmployee() && !cartBean.isSalesManager()) {
            LocalizedFacesMessage.error("ict.action-container.permission-denied");
            return;
        }
        cartBean.setCount(productRow.getCounter());
        cartBean.addToCart(productRow.getProduct());
        productRow.setCounter(1);

    }

    public Double getPrice(String productId) {
        return pricesByProducts.get(productId);
    }

    public boolean isComparison(ProductRow productRow) {
        return productRow.getComparison() != null;
    }
}
