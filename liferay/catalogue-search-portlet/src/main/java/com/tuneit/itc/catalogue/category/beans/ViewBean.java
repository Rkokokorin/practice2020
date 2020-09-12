package com.tuneit.itc.catalogue.category.beans;



import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
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
import com.liferay.portal.kernel.util.StringUtil;

import com.tuneit.itc.catalogue.cart.CartBean;
import com.tuneit.itc.catalogue.model.Category;
import com.tuneit.itc.catalogue.model.Manufacturer;
import com.tuneit.itc.catalogue.model.converters.CategoryConverter;
import com.tuneit.itc.catalogue.model.converters.ManufacturerConverter;
import com.tuneit.itc.catalogue.util.ProductLazyDataModel;
import com.tuneit.itc.catalogue.util.ProductRow;
import com.tuneit.itc.catalogue.util.URLEncoder;
import com.tuneit.itc.commons.URLConstants;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.ComparisonProduct;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.FavoriteProduct;
import com.tuneit.itc.commons.model.rest.MarketPrice;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ProductTypeResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.ComparisonProductService;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.FavoriteProductService;
import com.tuneit.itc.commons.service.WarehouseService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.FunctionUtils;
import com.tuneit.itc.commons.util.HttpUtil;
import com.tuneit.itc.commons.util.OffersUtil;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;


@Data
@ManagedBean
@ViewScoped
public class ViewBean {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{categoriesListBean}")
    private CategoriesListBean categoriesListBean;
    @ManagedProperty("#{categoriesListBean.categories}")
    private List<Category> categories;
    @ManagedProperty("#{categoriesListBean.rootCategories}")
    private Map<String, Category> rootCategories;
    @ManagedProperty("#{manufacturersBean}")
    private ManufacturersBean manufacturersBean;
    @ManagedProperty("#{manufacturersBean.manufacturers}")
    private List<Manufacturer> manufacturers;
    @ManagedProperty("#{manufacturersBean.manufacturersMap}")
    private Map<String, Manufacturer> manufacturersMap;
    @ManagedProperty("#{requester.productTypesService}")
    private Requester.ProductTypesService productTypesService;
    @ManagedProperty("#{requester.marketPricesService}")
    private Requester.MarketPricesService marketPricesService;
    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{favoriteProductService}")
    private FavoriteProductService favoriteProductService;
    @ManagedProperty("#{comparisonProductService}")
    private ComparisonProductService comparisonProductService;
    @ManagedProperty("#{cartBean}")
    private CartBean cartBean;
    @ManagedProperty("#{warehouseService}")
    private WarehouseService warehouseService;

    private Currency preferredCurrency;
    private Map<String, Double> pricesByProducts;
    private Map<String, Double> specialPricesByProducts;
    private List<ProductResponse.ProductHitSource> products;
    private Category selectedCategory;
    private Category selectedSubcategory;
    private Manufacturer selectedManufacturer;
    private String selectedProductId;

    private String selectedCategoryIdParamName = ParamNameConstants.CATEGORY_GROUP;
    private String selectedSubcategoryIdParamName = ParamNameConstants.CATEGORY_SUBGROUP;
    private String selectedManufacturerIdParamName = ParamNameConstants.MANUFACTURER;
    private String searchQueryParamName = ParamNameConstants.QUERY;
    private String pnsParamName = ParamNameConstants.PARTNUMBER_SEARCH;
    private String selectedProductIdParamName = ParamNameConstants.PRODUCT;
    private String plpageParamName = ParamNameConstants.PLPAGE;
    private String plrowsParamName = ParamNameConstants.PLROWS;
    private String warehouseParamName = ParamNameConstants.WAREHOUSE;

    private Boolean inStock;
    private Boolean weeklyDelivery;
    private Boolean excludeDeprecated;
    private ProductLazyDataModel productLazyDataModel;
    private CategoryConverter categoryConverter;
    private CategoryConverter categoryItemConverter;
    private ManufacturerConverter manufacturerConverter;
    private Integer currentTablePage = 0;
    private Boolean pageIsLast = false;
    private Integer currentTableRows = 20;

    private Filters filters;

    private boolean signedIn;
    private long userId;
    private String heartSvg;
    private String compareSvg;
    private String pageUrl;

    public void pageEventListener(PageEvent pe) {
        currentTablePage = pe.getPage();
        currentTableRows = ((DataTable) pe.getComponent()).getRows();
        pageIsLast = currentTablePage.equals(((DataTable) pe.getComponent())
            .getPageCount() - 1);
    }

    @PostConstruct
    public void init() {
        this.filters = new Filters();
        pageUrl = LiferayPortletHelperUtil.getThemeDisplay().getURLCurrent();
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            var heart = ec.getResourceAsStream("/WEB-INF/resources/images/heart.svg");
            var compare = ec.getResourceAsStream("/WEB-INF/resources/images/compare.svg");
            heartSvg = StringUtil.read(heart);
            compareSvg = StringUtil.read(compare);
        } catch (Exception exc) {
            log.error("Error while svg initialization!", exc);
        }
        filters.selectedCategoryId = "";
        filters.selectedSubcategoryId = "";
        filters.selectedManufacturerId = "";
        filters.searchQuery = "";
        pricesByProducts = new HashMap<>();
        specialPricesByProducts = new HashMap<>();

        categoryConverter = new CategoryConverter(rootCategories);
        categoryItemConverter = new CategoryConverter(new HashMap<>());
        manufacturerConverter = new ManufacturerConverter(manufacturersMap);

        final String categoryGroupId = HttpUtil.getRequestParam(ParamNameConstants.CATEGORY_GROUP);
        final String categoryId = HttpUtil.getRequestParam(ParamNameConstants.CATEGORY_SUBGROUP);
        final String manId = HttpUtil.getRequestParam(ParamNameConstants.MANUFACTURER);
        filters.searchQuery = HttpUtil.getRequestParam(ParamNameConstants.QUERY);
        String pns = HttpUtil.getRequestParam(ParamNameConstants.PARTNUMBER_SEARCH);

        filters.searchQuery = filters.searchQuery == null ? "" : filters.searchQuery;
        filters.partNumberSearch = pns != null && pns.equalsIgnoreCase("true");

        this.signedIn = LiferayPortletHelperUtil.getThemeDisplay().isSignedIn();
        this.userId = LiferayPortletHelperUtil.getUserId();

        productLazyDataModel = new ProductLazyDataModel(productsService, comparisonProductService,
            favoriteProductService, userId, this.filters, new ArrayList<>());

        if (categoryGroupId != null && rootCategories.get(categoryGroupId) != null) {
            selectedCategory = rootCategories.get(categoryGroupId);
            filters.selectedCategoryId = selectedCategory.getId();
            loadSubcategories();
        }
        if (categoryId != null && selectedCategory != null) {
            Optional<Category> first = selectedCategory.getCategoryItems()
                .stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst();
            if (first.isPresent()) {
                selectedSubcategory = first.get();
                filters.selectedSubcategoryId = selectedSubcategory.getId();
            }
        }
        if (manId != null && manufacturersMap.get(manId) != null) {
            selectedManufacturer = manufacturersMap.get(manId);
            filters.selectedManufacturerId = selectedManufacturer.getId();
        }
        inStock = false;
        weeklyDelivery = false;
        excludeDeprecated = false;
        preferredCurrency = currencyService.getUserCurrency();

        DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
            .findComponent("catalogueForm:listDataTable");
        if (d != null) {
            int rows = 20;
            String plrows = HttpUtil.getRequestParam("plrows");
            if (null != plrows) {
                try {
                    int plrowsint = Integer.parseInt(plrows);
                    if (/*plrowsint == 10 || */plrowsint == 20/* || plrowsint == 30*/) {
                        rows = plrowsint;
                    }
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
            if (page < 0) {
                page = 0;
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
            Map<String, Double> requestedPrices = body.stream().filter(it -> it.getViewPrice() != null)
                .collect(Collectors.toMap(MarketPrice::getNomenclatureCode,
                    MarketPrice::getViewPrice, FunctionUtils::fst)
                );
            log.debug("Newly loaded prices: {0}", requestedPrices);
            pricesByProducts.putAll(requestedPrices);

            specialPricesByProducts = body.stream().filter(it -> it.getPersonalPrice() != null)
                    .collect(Collectors.toMap(MarketPrice::getNomenclatureCode,
                            MarketPrice::getPersonalPrice, FunctionUtils::fst)
                    );
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

    public void updateWarehouse() {
        Warehouse selectedWarehouse = warehouseService.getUserWarehouse();
        filters.selectedWarehouseCode = selectedWarehouse.getCode();

    }

    public void toggleComparison(ProductRow productRow) {
        if (checkSignedInAndRedirect()) {
            return;
        }
        if (!cartBean.isContractorEmployee() && !cartBean.isSalesManager()) {
            LocalizedFacesMessage.error("ict.action-container.permission-denied");
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
        if (checkSignedInAndRedirect()) {
            return;
        }
        if (!cartBean.isContractorEmployee() && !cartBean.isSalesManager()) {
            LocalizedFacesMessage.error("ict.action-container.permission-denied");
            return;
        }
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

    private boolean checkSignedInAndRedirect() {
        if (!isSignedIn()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(URLConstants.LOGIN
                    + "?redirect=" + URLEncoder.encode(pageUrl)
                );
            } catch (IOException e) {
                log.error(e);
            }
            return true;
        }
        return false;
    }


    public Double getPrice(String productId) {
        return pricesByProducts.get(productId);
    }

    public boolean hasSpecialPrice(String productId) {
        Double marketPrice = pricesByProducts.get(productId);
        Double personalPrice = specialPricesByProducts.get(productId);
        return personalPrice != null && personalPrice > 0 && personalPrice < marketPrice;
    }

    public String renderSpecialPrice(String productId) {
        return "\n" + specialPricesByProducts.get(productId) + " " + preferredCurrency.getSign();
    }

    public List<Category> completeCategory(String query) {
        var result = categoriesListBean.searchGroup(query);
        categoryConverter = new CategoryConverter(
            result.stream().collect(Collectors.toMap(Category::getId, category -> category))
        );
        return result;
    }

    public void selectCategory() {
        if (selectedCategory != null) {
            categoryConverter = new CategoryConverter(rootCategories);
            if (selectedCategory.isSubcategory()) {
                selectedSubcategory = selectedCategory;
                selectedCategory = rootCategories.get(selectedCategory.getParentId());
                filters.selectedSubcategoryId = selectedSubcategory.getId();
                filters.selectedCategoryId = selectedCategory.getId();
            } else {
                filters.selectedCategoryId = selectedCategory.getId();
                this.selectedSubcategory = null;
                this.filters.selectedSubcategoryId = "";
            }
            loadSubcategories();
        } else {
            filters.selectedCategoryId = "";
            this.selectedSubcategory = null;
            this.filters.selectedSubcategoryId = "";
            categoryItemConverter = new CategoryConverter(new HashMap<>());
        }
        updatePageUrlParam(selectedCategoryIdParamName, filters.selectedCategoryId);
        pageUrl = pageUrl.replaceAll(selectedSubcategoryIdParamName + "=[^&]+", "" + filters.selectedCategoryId);
        resetProductTable();
    }

    private void loadSubcategories() {
        categoryItemConverter = new CategoryConverter(new HashMap<>());
        long size = 6000;
        long from = 0;

        Set<Category> itemsList = new TreeSet<>(Comparator.comparing(Category::getName));
        Map<String, Category> itemsMap = new HashMap<>();

        var productTypeHitSourceHits = requestByParent(filters.selectedCategoryId, size, from);
        if (productTypeHitSourceHits != null) {
            var hits = productTypeHitSourceHits.getHits();
            long total = productTypeHitSourceHits.getTotal().getValue();

            if (total > size) {
                from = size;
                size = total - size;
                var allTypes = requestByParent(filters.selectedCategoryId, size, from);
                if (allTypes != null) {
                    hits.addAll(allTypes.getHits());
                }
            }

            for (ResponseBase.Hit<ProductTypeResponse.ProductTypeHitSource> hit : hits) {
                ProductTypeResponse.ProductTypeHitSource source = hit.getSource();
                if (source.getSortingOrder() < 0) {
                    continue;
                }
                Category item = new Category(source.getName().trim(), source.getCode(),
                    source.getAssociatedProducts(), false, source.getParent(), true);
                itemsMap.put(source.getCode(), item);
                itemsList.add(item);
            }
        }
        selectedCategory.setCategoryItems(itemsList);
        categoryItemConverter = new CategoryConverter(itemsMap);
    }

    public void resetCategory() {
        selectedCategory = null;
        selectCategory();
    }

    public List<Category> completeSubcategory(String query) {
        return categoriesListBean.searchSubcategory(query, filters.selectedCategoryId);
    }

    public void selectSubcategory() {
        if (selectedSubcategory != null) {
            filters.selectedSubcategoryId = selectedSubcategory.getId();
        } else {
            filters.selectedSubcategoryId = "";
        }
        updatePageUrlParam(selectedSubcategoryIdParamName, filters.selectedSubcategoryId);
        resetProductTable();
    }

    public void resetSubcategory() {
        selectedSubcategory = null;
        selectSubcategory();
    }

    public List<Manufacturer> completeManufacturer(String query) {
        String queryLowerCase = query.toLowerCase();
        return manufacturersBean.searchManufacturers(queryLowerCase);
    }

    public void selectManufacturer() {
        if (selectedManufacturer != null) {
            filters.selectedManufacturerId = selectedManufacturer.getId();
        } else {
            filters.selectedManufacturerId = "";
        }
        updatePageUrlParam(selectedManufacturerIdParamName, filters.selectedManufacturerId);
        resetProductTable();
    }

    public void resetManufacturer() {
        selectedManufacturer = null;
        selectManufacturer();
    }

    public void resetProductTable() {
        DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
            .findComponent("catalogueForm:listDataTable");
        d.reset();
        currentTablePage = 0;
    }

    public String openProduct(ProductResponse.ProductHitSource productHitSource) {
        return String.format("/catalogue?faces-redirect=true&%s=%s",
            selectedProductIdParamName, productHitSource.getCode()
        );
    }

    private void updatePageUrlParam(String paramName, String paramValue) {
        if (pageUrl.contains(paramName)) {
            pageUrl = pageUrl.replaceAll(paramName + "=[^&]+", paramName + "=" + paramValue);
        } else {
            char lastChar = pageUrl.toCharArray()[pageUrl.toCharArray().length - 1];
            if (lastChar != '/' && lastChar != '?') {
                pageUrl += "&";
            }
            pageUrl += paramName + "=" + paramValue;
        }
    }

    public String getProductPictureUrl(ProductResponse.ProductHitSource product) {
        return WindowsPathNormalizer.getProductImageUrl(product);
    }

    private ResponseBase.Hits<ProductTypeResponse.ProductTypeHitSource> requestByParent(String parentId,
                                                                                        long size, long from) {
        Response<ProductTypeResponse> response;
        try {
            response = productTypesService.getByParent(parentId, from, size).execute();
            if (response.code() != 200) {
                log.error("Can't fetch product types. Response code is {0,number,integer}. Message {1}",
                    response.code(), response.message());
                return null;
            }
            ProductTypeResponse body = response.body();
            if (body == null) {
                log.error("Can't fetch product types. Response body is null");
                return null;
            }
            return body.getHits();
        } catch (IOException e) {
            log.error("Can't fetch product types. " + e.getMessage());
            log.error(e);
        }
        return null;
    }

    public void addToCart(ProductRow productRow) {
        if (checkSignedInAndRedirect()) {
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

    public boolean isFavorite(ProductRow productRow) {
        return productRow.getFavorite() != null;
    }

    public boolean isComparison(ProductRow productRow) {
        return productRow.getComparison() != null;
    }

    @Data
    public static class Filters implements Serializable {

        private Boolean partNumberSearch;
        private String searchQuery;
        private String selectedSubcategoryId;
        private String selectedCategoryId;
        private String selectedManufacturerId;
        private String selectedWarehouseCode;

        public Filters() {
        }

        public Filters(Filters filters) {
            this.partNumberSearch = filters.getPartNumberSearch();
            this.searchQuery = filters.getSearchQuery();
            this.selectedSubcategoryId = filters.getSelectedSubcategoryId();
            this.selectedCategoryId = filters.getSelectedCategoryId();
            this.selectedManufacturerId = filters.getSelectedManufacturerId();
            this.selectedWarehouseCode = filters.getSelectedWarehouseCode();
        }

    }
}
