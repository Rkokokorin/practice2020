package com.tuneit.itc.catalogue.category.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.util.StringUtil;

import com.tuneit.itc.catalogue.cart.CartBean;
import com.tuneit.itc.catalogue.util.ReplacementProductView;
import com.tuneit.itc.catalogue.util.URLEncoder;
import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.URLConstants;
import com.tuneit.itc.commons.jsf.ByIdConverter;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.ComparisonProduct;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.FavoriteProduct;
import com.tuneit.itc.commons.model.rest.AttachedFile;
import com.tuneit.itc.commons.model.rest.ManufacturerResponse;
import com.tuneit.itc.commons.model.rest.MarketPrice;
import com.tuneit.itc.commons.model.rest.PackingUnit;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.model.rest.SalesOffer;
import com.tuneit.itc.commons.model.rest.SalesOffersResponse;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.ComparisonProductService;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.FavoriteProductService;
import com.tuneit.itc.commons.service.WarehouseService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.FunctionUtils;
import com.tuneit.itc.commons.util.HttpUtil;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.commons.util.OffersUtil;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;


@Data
@ManagedBean
@ViewScoped
public class ProductViewBean implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(ProductViewBean.class);
    private final String selectedProductIdParamName = ParamNameConstants.PRODUCT;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{favoriteProductService}")
    private FavoriteProductService favoriteProductService;
    @ManagedProperty("#{comparisonProductService}")
    private ComparisonProductService comparisonProductService;
    @ManagedProperty("#{requester.salesOffersService}")
    private Requester.SalesOffersService salesOffersService;
    @ManagedProperty("#{requester.marketPricesService}")
    private Requester.MarketPricesService marketPricesService;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{warehouseService}")
    private WarehouseService warehouseService;
    @ManagedProperty("#{requester.manufacturerService}")
    private Requester.ManufacturerService manufacturerService;
    private String userCurrencySign;
    private Warehouse selectedWarehouse;
    private Currency userCurrency;
    @ManagedProperty("#{cartBean}")
    private CartBean cartBean;
    private SalesOffersResponse salesOffersResponse;
    private List<SalesOffer> salesOffers;
    private SalesOffer selectedOffer;
    private ByIdConverter<SalesOffer> offerConverter;
    private ProductResponse.ProductHitSource product;
    private List<ReplacementProductView> replacements;
    private boolean signedIn;
    private FavoriteProduct favoriteProduct;
    private ComparisonProduct comparisonProduct;
    private long userId;
    private String productCode;
    private long price = 640;
    private String heartSvg;
    private String compareSvg;
    private String cartSvg;
    private String warehouseSvg;
    private String pageUrl;

    private Map<String, Long> manufacturerSorting = new HashMap<>();
    private List<String> daysWords = new ArrayList<>(Arrays.asList("день", "дня", "дней"));

    private String warehouseParamName = ParamNameConstants.WAREHOUSE;
    private String selectedWarehouseCode;

    @PostConstruct
    public void init() {
        selectedWarehouse = warehouseService.getUserWarehouse();
        userCurrency = currencyService.getUserCurrency();
        userCurrencySign = userCurrency.getSign();
        pageUrl = LiferayPortletHelperUtil.getThemeDisplay().getURLCurrent();
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            final var heart = ec.getResourceAsStream("/WEB-INF/resources/images/heart.svg");
            final var compare = ec.getResourceAsStream("/WEB-INF/resources/images/compare.svg");
            final var cart = ec.getResourceAsStream("/WEB-INF/resources/images/cart.svg");
            final var warehouseCart = ec.getResourceAsStream("/WEB-INF/resources/images/warehouse.svg");
            heartSvg = StringUtil.read(heart);
            compareSvg = StringUtil.read(compare);
            cartSvg = StringUtil.read(cart);
            warehouseSvg = StringUtil.read(warehouseCart);
        } catch (Exception exc) {
            log.error("Error while svg initialization!", exc);
        }
        this.signedIn = LiferayPortletHelperUtil.getThemeDisplay().isSignedIn();
        this.userId = LiferayPortletHelperUtil.getUserId();
        this.productCode = HttpUtil.getRequestParam(selectedProductIdParamName);
        if (productCode == null) {
            return;
        }
        try {
            log.trace("Execute product service call");
            Response<ProductResponse> response = productsService.getById(productCode, true).execute();
            ResponseBase.Total total = response.body().getHits().getTotal();
            if (total.getValue() != 1) {
                return;
            }
            this.product = response.body().getHits().getHits().get(0).getSource();
            if (isSignedIn()) {
                favoriteProduct = favoriteProductService.findFavorite(userId, productCode);
                comparisonProduct = comparisonProductService.findComparison(userId, productCode);
            }
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage());
        }
        String wh  = HttpUtil.getRequestParam(warehouseParamName);
        if (wh != null) {
            selectedWarehouseCode = wh;
        }
        updateSalesOffers();
        selectOffer();
        this.replacements = loadReplacements(product);
    }

    public Boolean hasAnalogs() {
        return replacements != null && !replacements.isEmpty();
    }

    public String openProduct(ProductResponse.ProductHitSource product) throws PortalException {
        LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
        catalogueUrl.setParameter(ParamNameConstants.PRODUCT, product.getCode());
        return catalogueUrl.toString();
    }

    public String getProductCodeImageUrl(ProductResponse.ProductHitSource product) {
        return WindowsPathNormalizer.getProductImageUrl(product);
    }

    public void selectOffer() {
        if (selectedOffer != null) {
            cartBean.setCount(getMinCount());
        }
    }

    public Long getMaxCount() {
        return selectedOffer.getStockAmount() != 0 ? selectedOffer.getStockAmount() : Long.MAX_VALUE;
    }

    public Long getMinCount() {
        if (selectedOffer.getStockAmount() == 0) {
            return Long.valueOf(selectedOffer.getMinimalOrder());
        }
        return Long.valueOf(selectedOffer.getMinimalOrder() > selectedOffer.getStockAmount()
            ? selectedOffer.getStockAmount()
            : selectedOffer.getMinimalOrder());
    }

    public void countChanged() {
        if (cartBean.getCount() > getMaxCount()) {
            cartBean.setCount(getMaxCount());
            return;
        }
        if (cartBean.getCount() < getMinCount()) {
            cartBean.setCount(getMinCount());
            return;
        }
        if (cartBean.getCount() % selectedOffer.getMultiplicity() != 0) {
            cartBean.setCount(cartBean.getCount() - (cartBean.getCount() % selectedOffer.getMultiplicity())
                + selectedOffer.getMultiplicity());
        }
    }

    public String getManufacturerProductsUrl() {
        if (product == null) {
            return "";
        }
        String catalogueBaseUrl;
        try {
            LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
            catalogueBaseUrl = catalogueUrl.toString();
            catalogueBaseUrl = catalogueBaseUrl.substring(0, catalogueUrl.toString().indexOf('?'));
        } catch (PortalException e) {
            log.error("Can't get catalogue base url. ", e);
            return "";
        }
        return catalogueBaseUrl + "?"
            + ParamNameConstants.MANUFACTURER + "=" + this.product.getManufacturer().getCode();
    }

    public long getManufacturerSorted() {
        if (manufacturerSorting.get(product.getManufacturer().getCode()) == null) {
            ResponseBase.Hit<ManufacturerResponse.ManufacturerHitSource> manufacturerHit =
                findManufacturerById(product.getManufacturer().getCode());
            if (manufacturerHit == null || manufacturerHit.getSource() == null) {
                manufacturerSorting.put(product.getManufacturer().getCode(), -1L);
            } else {
                manufacturerSorting.put(product.getManufacturer().getCode(),
                    manufacturerHit.getSource().getSortingOrder());
            }
        }
        return manufacturerSorting.get(product.getManufacturer().getCode());
    }

    private List<ReplacementProductView> loadReplacements(ProductResponse.ProductHitSource product) {
        if (product.getReplacements() == null || product.getReplacements().isEmpty()) {
            return null;
        }
        List<String> replacementIds = product.getReplacements().stream()
            .map(ProductResponse.ProductReplacement::getProductCode)
            .collect(Collectors.toList());
        log.trace("Execute product service call");
        Call<List<ProductResponse>> replacementsCall = productsService.getByIds(replacementIds);
        Map<String, MarketPrice> pricesById;
        List<MarketPrice> marketPrices = pricesByIds(replacementIds);
        if (marketPrices != null) {
            pricesById = marketPrices.stream()
                .collect(Collectors.toMap(MarketPrice::getNomenclatureCode, x -> x, FunctionUtils::fst));
        } else {
            pricesById = new HashMap<>();
        }
        try {
            Response<List<ProductResponse>> execute = replacementsCall.execute();
            List<ProductResponse> body = execute.body();
            if (body == null || !execute.isSuccessful()) {
                log.warn("Error in replacements call response, code {0}", execute.code());
                return null;
            }
            return body.stream()
                .map(ProductResponse::getHits)
                .map(ResponseBase.Hits::getHits)
                .flatMap(List::stream)
                .map(ResponseBase.Hit::getSource)
                .map(prod -> new ReplacementProductView(prod, pricesById.get(prod.getCode())))
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Can not load replacements, {0}", e.getMessage());
            log.debug("Exception: ", e);
            return null;
        }
    }

    private List<MarketPrice> pricesByIds(List<String> ids) {
        if (userCurrency == null) {
            log.warn("User currency is null!");
            return null;
        }
        try {
            Response<List<MarketPrice>> execute = marketPricesService.getByIds(ids, userCurrency,
                    OffersUtil.getCustomerId()).execute();
            List<MarketPrice> body = execute.body();
            if (body == null || !execute.isSuccessful()) {
                log.warn("Unexpected wrong body. Code {0}", execute.code());
            }
            return body;
        } catch (IOException e) {
            log.warn("Can not fetch market prices! {0}", e.getMessage());
            log.debug("Exception: ", e);
            return null;
        }
    }

    private ResponseBase.Hit<ManufacturerResponse.ManufacturerHitSource> findManufacturerById(String manufacturerId) {
        try {
            Response<ManufacturerResponse> response = manufacturerService.getById(manufacturerId).execute();
            if (response.code() != 200) {
                log.error("Can't fetch manufacturer by id. Response code is " + response.code() +
                    ". Message " + response.message());
                return null;
            }
            ManufacturerResponse body = response.body();
            if (body == null) {
                log.error("Can't fetch manufacturer. Response body is null");
                return null;
            }
            var hitList = body.getHits().getHits();
            if (hitList.isEmpty()) {
                log.error("Can't fetch manufacturer. Hits is empty");
                return null;
            }
            return hitList.get(0);
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    public boolean isFavorite() {
        return favoriteProduct != null;
    }

    public boolean isComparison() {
        return comparisonProduct != null;
    }

    public void toggleComparison() {
        if (checkSignedInAndRedirect()) {
            return;
        }
        if (!cartBean.isContractorEmployee() && !cartBean.isSalesManager()) {
            LocalizedFacesMessage.error("actionContainerMessage", "ict.action-container.permission-denied", "");
            return;
        }

        if (comparisonProduct == null) {
            ComparisonProduct cp = new ComparisonProduct();
            cp.setUserId(userId);
            cp.setProductCode(productCode);
            this.comparisonProduct = comparisonProductService.save(cp);
        } else {
            this.comparisonProduct = comparisonProductService.attachToManager(this.comparisonProduct);
            comparisonProductService.delete(comparisonProduct);
            this.comparisonProduct = null;
        }
    }

    public void toggleFavorite() {
        if (checkSignedInAndRedirect()) {
            return;
        }
        if (!cartBean.isContractorEmployee() && !cartBean.isSalesManager()) {
            LocalizedFacesMessage.error("ict.action-container.permission-denied");
            return;
        }

        if (productCode == null) {
            return;
        }
        if (favoriteProduct == null) {
            FavoriteProduct fp = new FavoriteProduct();
            fp.setUserId(userId);
            fp.setProductCode(productCode);
            this.favoriteProduct = favoriteProductService.save(fp);
        } else {
            this.favoriteProduct = favoriteProductService.attachToManager(this.favoriteProduct);
            favoriteProductService.delete(favoriteProduct);
            this.favoriteProduct = null;
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

    public List<String> getProductImagesUrl() {
        List<String> result = new ArrayList<>();
        result.add(URLEncoder.encode(WindowsPathNormalizer.getProductImageUrl(this.product)));
        result.addAll(WindowsPathNormalizer
            .getAllProductImagesUrl(this.product, false)
            .stream()
            .map(URLEncoder::encode)
            .collect(Collectors.toList()));
        return result;
    }

    public String convertToUrl(AttachedFile file) {
        return WindowsPathNormalizer.normalizeUrl(file.getFileType().getName(), file.getFilePath());
    }

    public String getFilename(AttachedFile file) {
        return WindowsPathNormalizer.extractName(file.getFilePath());
    }

    public List<FileGroup> getAttachedFiles() {

        if (product.getAttachedFiles() == null) {
            return new ArrayList<>();
        }
        return product.getAttachedFiles()
            .stream()
            .filter(file -> !file.isPicture())
            .filter(file -> file.getFileType().equals(AttachedFile.FileType.REGULAR_FILE))
            .peek(file -> {
                if (file.getFileKind() == null || file.getFileKind().isBlank()) {
                    file.setFileKind("");
                }
            })
            .collect(Collectors.groupingBy(AttachedFile::getFileKind))
            .entrySet()
            .stream()
            .map(entry -> new FileGroup(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(FileGroup::getGroupName))
            .collect(Collectors.toList());
    }

    public void updateCurrency() {
        selectedWarehouse = warehouseService.getUserWarehouse();
        selectedWarehouseCode = selectedWarehouse.getCode();
        userCurrency = currencyService.getUserCurrency();
        userCurrencySign = userCurrency.getSign();
        updateSalesOffers();

    }

    public List<ProductResponse.ProductHitSource.ProductFeature> getProductFeatures() {
        List<ProductResponse.ProductHitSource.ProductFeature> features = product.getFeatures();
        if (features == null || features.isEmpty()) {
            return new ArrayList<>();
        }
        features.sort(Comparator.comparing(ProductResponse.ProductHitSource.ProductFeature::getName));
        return features;
    }

    public void updateSalesOffers() {
        SalesOffersResponse response = OffersUtil.loadOffers(productCode, salesOffersService,
                userCurrency, selectedWarehouse);
        if (response == null) {
            log.error("Can't fetch sales offers for Product: {0}, Warehouse: {1}, Currency: {2}",
                    productCode, selectedWarehouse.getCode(), userCurrency.getId());
            return;
        }
        salesOffersResponse = response;
        salesOffers = salesOffersResponse.getSalesOffers().stream().filter(so -> so.getPrice() != null)
                .sorted(Comparator.comparing(SalesOffer::getPrice)).collect(Collectors.toList());
        offerConverter = new ByIdConverter<>(salesOffers, SalesOffer::getSalesOfferId);
        if (!salesOffers.isEmpty()) {
            selectedOffer = salesOffers.get(0);
        } else {
            selectedOffer = null;
        }
    }

    public boolean renderAllSpecialPrice() {
        return salesOffersResponse.getSalesOffers().stream().anyMatch(this::renderSpecialPrice);
    }

    public boolean renderSpecialPrice(SalesOffer offer) {
        double personalPrice = offer.getPersonalPrice();
        return personalPrice > 0;
    }

    public int getAsDays(int hours) {
        return hours / 24;
    }

    public Boolean displayBasePacking() {
        if (selectedOffer == null) {
            return false;
        }
        Optional<SalesOffer> selectedOfferOpt = Optional.of(this.selectedOffer);
        Optional<PackingUnit> packingUnit = selectedOfferOpt.map(SalesOffer::getPackingUnit);
        String packingUnitCode = packingUnit.map(PackingUnit::getCode).orElse(null);
        String basePackingUnit = packingUnit.map(PackingUnit::getBasePackingUnit)
            .map(PackingUnit::getCode).orElse(null);
        return !Objects.equals(packingUnitCode, basePackingUnit) ||
            packingUnit.map(PackingUnit::getMultiplier).map(mul -> mul > 1).orElse(false);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileGroup {
        private String groupName;
        private List<AttachedFile> files;
    }
}
