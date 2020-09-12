package com.tuneit.itc.search.beans;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.rest.ProductTypeResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.search.model.Product;
import com.tuneit.itc.search.model.ProductType;

@Data
@ManagedBean
@ViewScoped
public class ViewBean {
    private static final String INLINE_BLOCK = "inline-block";
    private static final String NONE = "none";

    private final String selectedCategoryIdParamName = ParamNameConstants.CATEGORY_GROUP;
    private final String selectedSubcategoryIdParamName = ParamNameConstants.CATEGORY_SUBGROUP;
    private final String selectedProductIdParamName = ParamNameConstants.PRODUCT;
    private final String searchQueryParamName = ParamNameConstants.QUERY;
    private final Log log = LogFactoryUtil.getLog(this.getClass());
    private final Locale ruLocale = new Locale("ru", "RU");
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{requester.productTypesService}")
    private Requester.ProductTypesService productTypesService;
    @ManagedProperty("#{requester.manufacturerService}")
    private Requester.ManufacturerService manufacturerService;
    private List<ProductType> productTypeRes = new ArrayList<>();
    private List<Product> productRes = new ArrayList<>();
    private String searchQuery;
    private String searchDisplayState;
    private String catalogueBaseUrl = "/catalogue";

    @PostConstruct
    public void init() {
        searchQuery = "";
        searchDisplayState = NONE;
        try {
            LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
            catalogueBaseUrl = catalogueUrl.toString();
            catalogueBaseUrl = catalogueBaseUrl.substring(0, catalogueUrl.toString().indexOf('?'));
        } catch (PortalException e) {
            log.error("Can't get catalogue base url. " + e.getMessage());
            log.error(e);
        }
    }


    public void updateResult() {
        if (searchQuery.length() > 0) {
            ThemeDisplay td = LiferayPortletHelperUtil.getThemeDisplay();
            CompletableFuture<List<ProductType>> productTypesRequest =
                CompletableFuture.supplyAsync(() -> this.updateProductTypes(td));
            CompletableFuture<List<Product>> productsRequest =
                CompletableFuture.supplyAsync(() -> this.updateProducts(td));
            try {
                this.productTypeRes = productTypesRequest.get();
                this.productRes = productsRequest.get();
                searchDisplayState = INLINE_BLOCK;
            } catch (InterruptedException e) {
                log.warn(e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException exc) {
                log.warn(exc);
            }
        } else {
            productTypeRes = new ArrayList<>();
            productRes = new ArrayList<>();
            searchDisplayState = NONE;
        }
    }

    private List<ProductType> updateProductTypes(ThemeDisplay themeDisplay) {
        long id = Thread.currentThread().getId();
        log.debug("Current thread " + id);
        List<ProductType> productTypeRes = new ArrayList<>();
        try {
            Response<ProductTypeResponse> response = productTypesService.query(searchQuery, 0, 11).execute();
            if (!response.isSuccessful()) {
                log.error("Can't fetch product types. Response code: " + response.code()
                    + " Message: " + response.message());
                return productTypeRes;
            }
            ProductTypeResponse body = response.body();
            if (body == null) {
                log.error("Can't fetch product types. Body is null");
                return productTypeRes;
            }
            List<ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource>> hits = body.getHits().getHits();
            for (var h : hits) {
                ProductTypeResponse.ProductTypeHitSource source = h.getSource();
                Locale locale = themeDisplay.getLocale();
                String localizedName;
                if (locale.equals(ruLocale)) {
                    localizedName = source.getName();
                } else {
                    localizedName = source.getNameEng() == null || source.getNameEng().isBlank()
                        ? source.getName() : source.getNameEng();
                }
                if (source.isGroup()) {
                    productTypeRes.add(new ProductType(localizedName, source.getCode(), "", true));
                } else {
                    if (source.getAssociatedProducts() > 0) {
                        if (source.getParent() == null || source.getParent().isBlank()) {
                            productTypeRes.add(new ProductType(localizedName, source.getCode(), "", true));
                        } else {
                            productTypeRes.add(new ProductType(localizedName, source.getCode(),
                                source.getParent(), false));
                        }
                    }

                }
            }
        } catch (IOException e) {
            log.error("Can't fetch product types. " + e.getMessage());
            log.error(e);
        }
        return productTypeRes;
    }

    private List<Product> updateProducts(ThemeDisplay themeDisplay) {
        long id = Thread.currentThread().getId();
        log.debug("Current thread " + id);
        List<Product> productRes = new ArrayList<>();
        try {
            var response = productsService.query(searchQuery, 0, 11).execute();
            if (!response.isSuccessful()) {
                log.error("Can't fetch product by query: " + searchQuery + ". Response code: "
                    + response.code() + " Message: " + response.message());
                return productRes;
            }
            var body = response.body();
            if (body == null) {
                log.error("Can't fetch product by query. Body is null");
                return productRes;
            }
            if (body.size() < 1) {
                log.error("Can't fetch product by query. Body is empty");
                return productRes;
            }
            var hits = body.get(0).getHits().getHits();
            for (var h : hits) {
                var source = h.getSource();
                Locale locale = themeDisplay.getLocale();
                if (locale.equals(ruLocale)) {
                    productRes.add(new Product(source.getName(), source.getManufacturer().getName(),
                        source.getVendorCode(), source.getSecondVendorCode(), source.getNomenclatureType().getName(),
                        source.getCode()));
                } else {
                    String localizedName = source.getNameEng() == null || source.getNameEng().isBlank()
                        ? source.getName() : source.getNameEng();
                    productRes.add(new Product(localizedName, source.getManufacturer().getName(),
                        source.getVendorCode(), source.getSecondVendorCode(), source.getNomenclatureType().getName(),
                        source.getCode()));
                }
            }
        } catch (IOException e) {
            log.error("Can't fetch product by partnumber. " + e.getMessage());
            log.error(e);
        }
        return productRes;
    }

    public void goToCatalogue() throws IOException {
        String encodedParam = "";
        if (searchQuery != null && !searchQuery.isBlank()) {
            encodedParam = "?" + searchQueryParamName + "=" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
        }
        FacesContext.getCurrentInstance().getExternalContext().redirect(catalogueBaseUrl + encodedParam);
    }

    public boolean isAllResultsEmpty() {
        return productTypeRes.isEmpty() && productRes.isEmpty() && !searchQuery.isBlank();
    }

    public boolean isBlankQuery() {
        return searchQuery.isBlank();
    }
}
