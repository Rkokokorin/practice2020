package com.tuneit.itc.bom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;

import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.ByIdConverter;
import com.tuneit.itc.commons.jsf.LocalizedFacesMessage;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.jsf.RoleChecker;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.cart.Cart;
import com.tuneit.itc.commons.model.cart.CartPosition;
import com.tuneit.itc.commons.model.cart.ProductOffer;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ResponseBase;
import com.tuneit.itc.commons.model.rest.SalesOffer;
import com.tuneit.itc.commons.model.rest.SalesOffersResponse;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.WarehouseService;
import com.tuneit.itc.commons.service.cart.CartPositionService;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.commons.util.OffersUtil;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;

import static java.util.stream.Collectors.toList;

@Data
@ManagedBean(name = "bomSearchBean")
@ViewScoped
public class BOMSearchBean implements Serializable {

    private final transient Logger logger = LoggerFactory.getLogger(BOMSearchBean.class);
    private UploadedFile uploadedFile;

    private List<BOMSearchResponse> bomSearchResponse;

    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{cartService}")
    private CartService cartService;

    @ManagedProperty("#{cartPositionService}")
    private CartPositionService cartPositionService;

    @ManagedProperty("#{queryParserService}")
    private QueryParserService queryParserService;

    @ManagedProperty("#{requester.salesOffersService}")
    private Requester.SalesOffersService salesOffersService;

    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{warehouseService}")
    private WarehouseService warehouseService;

    private Currency preferredCurrency;
    private Warehouse selectedWarehouse;

    private List<String> daysWords = new ArrayList<>(Arrays.asList("день", "дня", "дней"));
    private Map<String, SalesOffersResponse> offersById;
    private ByIdConverter<ProductResponse.ProductHitSource> productResponseConverter;
    private ByIdConverter<SalesOffer> salesOfferConverter;

    private int rowId;

    @PostConstruct
    public void init() {
        rowId = 0;
        this.preferredCurrency = currencyService.getUserCurrency();
        this.selectedWarehouse = warehouseService.getUserWarehouse();
        this.bomSearchResponse = new ArrayList<>();
        this.bomSearchResponse.add(new BOMSearchResponse(rowId, new QueryParserService.BOMQueryRecord(), null, null));
        ++rowId;
        this.offersById = new HashMap<>();
        this.productResponseConverter = new ByIdConverter<>(new ArrayList<>(),
            ProductResponse.ProductHitSource::getCode);
        this.salesOfferConverter = new ByIdConverter<>(new ArrayList<>(), SalesOffer::getSalesOfferId);
    }

    public void upload(FileUploadEvent event) {
        uploadedFile = event.getFile();
        bomSearchResponse = new ArrayList<>();
        logger.info("User [{0,number,integer}] executes bom search", LiferayPortletHelperUtil.getUserId());
        List<QueryParserService.BOMQueryRecord> queryRecords;
        try {
            queryRecords = queryParserService.parseQuery(uploadedFile);
        } catch (QueryParserService.InvalidCsvRecordException exc) {
            logger.warn(exc.getMessage());
            LocalizedFacesMessage.error("itc.bom.csv.invalid-format");
            return;
        } catch (QueryParserService.CorruptedFileException corruptedFile) {
            logger.warn(corruptedFile.getMessage());
            LocalizedFacesMessage.error("itc.bom.file.corrupted");
            return;
        } catch (QueryParserService.EmptyFileException emptyFile) {
            logger.debug("Uploaded file was empty");
            LocalizedFacesMessage.error("itc.bom.file.empty");
            return;
        } catch (QueryParserService.InvalidHeaderException invalidHeader) {
            logger.info("Uploaded file hasn't required columns {0}", invalidHeader.getMissingColumnName());
            LocalizedFacesMessage.errorFormat("itc.bom.header.missing-column",
                new Object[] {invalidHeader.getMissingColumnName()});
            return;
        }
        if (queryRecords == null) {
            LocalizedFacesMessage.error("itc.bom.can-not-parse-csv");
            return;
        }

        bomSearchResponse = convertQueriesToSearchResponse(queryRecords);

    }

    public void searchByCurrentTable() {
        if (bomSearchResponse == null) {
            return;
        }

        List<QueryParserService.BOMQueryRecord> queryRecords = bomSearchResponse.stream()
            .map(BOMSearchResponse::getQueryRecord)
            .filter(Objects::nonNull)
            .collect(toList());


        var searchResponse = searchByParsedTable(queryRecords);
        if (searchResponse == null) {
            return;
        }
        List<ProductResponse.ProductHitSource> foundProducts = searchResponse.stream()
            .flatMap(Collection::stream)
            .collect(toList());
        productResponseConverter = new ByIdConverter<>(foundProducts, ProductResponse.ProductHitSource::getCode);

        rowId = 0;
        if (searchResponse.size() != queryRecords.size()) {
            logger.error("Error! There are response size does not match query. " +
                "Query size {0}, response size {1}", queryRecords.size(), searchResponse.size());
            if (logger.isDebugEnabled()) {
                logger.debug("Query records size: {0}, response: {1}", queryRecords.size(), searchResponse.size());
                logger.debug("Query records: {0}", queryRecords);
                List<List<String>> compactResponse = searchResponse.stream()
                    .map(resp -> resp.stream().map(ProductResponse.ProductHitSource::getCode).collect(toList()))
                    .collect(toList());
                logger.debug("Response: {0}", compactResponse);
            }
            LocalizedFacesMessage.error("itc.bom.response-does-not-match-query");
        } else {
            bomSearchResponse = combineSearchResultsAndQueries(searchResponse, queryRecords);
        }
        List<ProductResponse.ProductHitSource> selectedProducts = bomSearchResponse.stream()
            .map(BOMSearchResponse::getSelectedProduct)
            .filter(Objects::nonNull)
            .collect(toList());
        loadOffers(selectedProducts);

    }

    private List<BOMSearchResponse> convertQueriesToSearchResponse(List<QueryParserService.BOMQueryRecord> records) {
        int index = 0;
        List<BOMSearchResponse> responses = new ArrayList<>(records.size());
        for (QueryParserService.BOMQueryRecord record : records) {
            BOMSearchResponse bomSearchResponse = new BOMSearchResponse(index, record, new ArrayList<>(), null);
            responses.add(bomSearchResponse);
            ++index;
        }
        return responses;
    }

    private List<BOMSearchResponse> combineSearchResultsAndQueries(
            List<List<ProductResponse.ProductHitSource>> searchResponse,
            List<QueryParserService.BOMQueryRecord> queryRecords) {
        List<BOMSearchResponse> bomSearchResponse = new ArrayList<>();
        if (searchResponse.size() != queryRecords.size()) {
            logger.warn("Error! There are less responses than query strings");
        } else {
            bomSearchResponse = new ArrayList<>();
            for (int i = 0; i < queryRecords.size(); i++) {
                QueryParserService.BOMQueryRecord query = queryRecords.get(i);
                List<ProductResponse.ProductHitSource> queryResponse = searchResponse.get(i);
                ProductResponse.ProductHitSource selectedResponse = null;
                if (!queryResponse.isEmpty()) {
                    selectedResponse = queryResponse.get(0);
                }
                bomSearchResponse.add(new BOMSearchResponse(rowId, query, queryResponse, selectedResponse));
                ++rowId;
            }
        }
        return bomSearchResponse;
    }


    private List<List<ProductResponse.ProductHitSource>> searchByParsedTable(
            List<QueryParserService.BOMQueryRecord> bomQuery) {
        var call = productsService.bomQuery(bomQuery.stream()
            .map(qr -> new Requester.ProductsService.BOMQuery(qr.getManufacturer(), qr.getArticle(), qr.getQuery()))
            .collect(toList())
        );
        List<ProductResponse> body;
        try {
            var execute = call.execute();
            body = execute.body();
        } catch (IOException ioe) {
            logger.error("Unexpected error while query service: {0}", ioe.getMessage());
            LocalizedFacesMessage.error("itc.bom.can-not-execute-query");
            return null;
        }
        if (body == null) {
            logger.error("Unexpected null body for query");
            LocalizedFacesMessage.error("itc.bom.can-not-execute-query");
            return null;
        }
        return body.stream()
            .map(ProductResponse::getHits)
            .map(ResponseBase.Hits::getHits)
            .map(hits -> hits.stream().map(ResponseBase.Hit::getSource).collect(toList()))
            .collect(toList());
    }

    public void updateQuery(BOMSearchResponse search) {
        logger.debug("Update query in row {0}. {1}", search.getIndex(), search.getQueryRecord());
        Call<ProductResponse> call = productsService.bomQuery(search.queryRecord.getQuery(),
            search.queryRecord.getManufacturer(), search.queryRecord.getArticle());
        try {
            Response<ProductResponse> execute = call.execute();
            var hits = execute.body().getHits().getHits()
                .stream()
                .map(ResponseBase.Hit::getSource)
                .collect(toList());
            search.response = hits;
            search.selectedProduct = !hits.isEmpty() ? hits.get(0) : null;
            SalesOffersResponse offer = OffersUtil.loadOffers(search.getSelectedProduct().getProductId(),
                    salesOffersService, preferredCurrency, selectedWarehouse);
            this.productResponseConverter.addAll(hits);
            if (offer != null) {
                updateOffersConverter(Collections.singleton(offer));
            } else {
                LocalizedFacesMessage.error("itc.bom.can-not-fetch-offers");
            }
            this.resetOffer(search);
        } catch (IOException e) {
            logger.warn("Unexpected error", e);
            LocalizedFacesMessage.error("itc.bom.can-not-execute-query");
        }
    }

    public void removeRow(BOMSearchResponse row) {
        this.bomSearchResponse.removeIf(response -> response.getIndex() == row.getIndex());
    }

    public String productPictureUrl(ProductResponse.ProductHitSource product) {
        return WindowsPathNormalizer.getProductImageUrl(product);
    }

    @SneakyThrows
    public String openProduct(ProductResponse.ProductHitSource product) {
        LiferayPortletURL portletUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
        portletUrl.setParameter(ParamNameConstants.PRODUCT, product.getCode());
        return portletUrl.toString();
    }

    public String getDisplayName(ProductResponse.ProductHitSource product) {
        if (product.getShortName() != null && !product.getShortName().isBlank()) {
            return product.getShortName();
        } else if (product.getShortNameEng() != null && !product.getShortNameEng().isBlank()) {
            return product.getShortNameEng();
        } else {
            return product.getNomenclatureType().getName();
        }
    }

    public void addToCart() {
        if (!isHasCartAccess()) {
            LocalizedFacesMessage.error("itc.bom.has-no-cart-access");
        }
        Cart currentCart = getUserCart();

        var recordsToCreate = filterNotExistingPositions(bomSearchResponse, currentCart);
        List<CartPosition> positionsToSave = recordsToCreate.stream()
            .map(resp -> {
                String externalProductId = null;
                String productDescription = null;
                if (resp.selectedProduct == null) {
                    productDescription = resp.queryRecord.toString();
                } else {
                    externalProductId = resp.selectedProduct.getCode();
                }
                ProductOffer offer = null;
                if (resp.selectedOffer != null) {
                    offer = new ProductOffer(
                        UUID.fromString(resp.selectedOffer.getRequestId()),
                        UUID.fromString(resp.selectedOffer.getSalesOfferId()),
                        preferredCurrency.getId(), selectedWarehouse.getCode(), resp.selectedOffer.getPrice()
                    );
                }
                return new CartPosition(null, externalProductId, productDescription,
                    new Date(), resp.getAmount(), currentCart, offer);
            })
            .filter(cartPosition -> cartPosition.getExternalProductId() != null
                || cartPosition.getProductDescription() != null)
            .collect(toList());
        combineSamePositions(positionsToSave);
        cartPositionService.doInTransaction(em -> {
            for (CartPosition cartPosition : positionsToSave) {
                em.persist(cartPosition);
            }
        });
        LocalizedFacesMessage.info("itc.bom.added-to-cart");
    }

    public void reloadOffersOnWarehouseOrCurrencyChange() {
        this.preferredCurrency = currencyService.getUserCurrency();
        this.selectedWarehouse = warehouseService.getUserWarehouse();
        List<ProductResponse.ProductHitSource> products = bomSearchResponse.stream()
            .map(BOMSearchResponse::getSelectedProduct)
            .filter(Objects::nonNull)
            .collect(toList());
        this.offersById = null;
        loadOffers(products);
    }

    private void selectOfferByComparator(Comparator<SalesOffer> comparator) {
        if (this.bomSearchResponse != null) {
            for (var cartRow : this.bomSearchResponse) {
                List<SalesOffer> salesOffers = cartRow.getSalesOffers();
                if (salesOffers != null && !salesOffers.isEmpty()) {
                    SalesOffer fastest = salesOffers.stream().min(comparator).orElse(null);
                    cartRow.setSelectedOffer(fastest);
                }
            }
        }
    }

    public void selectCheapestOffers() {
        selectOfferByComparator(Comparator.comparing(SalesOffer::getPrice).thenComparing(SalesOffer::getDuration));
    }

    public void selectFastestOffers() {
        selectOfferByComparator(Comparator.comparing(SalesOffer::getDuration).thenComparing(SalesOffer::getPrice));
    }


    public boolean isHasCartAccess() {
        return RoleChecker.canAddToCart();
    }

    public String currencyFormat(double value) {
        return String.format("%,.2f", value);
    }

    private void loadOffers(List<ProductResponse.ProductHitSource> products) {
        List<SalesOffersResponse> offers = OffersUtil.loadOffers(products, salesOffersService,
            preferredCurrency, selectedWarehouse);
        if (offers == null) {
            logger.error("Offers was not loaded!");
            LocalizedFacesMessage.error("itc.bom.can-not-fetch-offers");
        } else {
            updateOffersConverter(offers);

            for (BOMSearchResponse searchResponse : bomSearchResponse) {
                ProductResponse.ProductHitSource selectedProduct = searchResponse.getSelectedProduct();
                if (selectedProduct != null) {
                    var offerForProduct = offersById.get(selectedProduct.getCode());
                    if (offerForProduct != null) {
                        List<SalesOffer> offersList = offerForProduct.getSalesOffers();
                        searchResponse.setSalesOffers(offersList);
                        if (offersList != null && !offersList.isEmpty()) {
                            searchResponse.setSelectedOffer(offersList.get(0));
                        }
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                for (BOMSearchResponse searchResponse : bomSearchResponse) {
                    logger.debug("Product with id {0}, offers count {1}, offers ids {2}",
                        Optional.ofNullable(searchResponse.getSelectedProduct())
                            .map(ProductResponse.ProductHitSource::getCode)
                            .orElse(null),
                        searchResponse.getSalesOffers().size(),
                        searchResponse.getSalesOffers().stream().map(SalesOffer::getSalesOfferId).collect(toList())
                    );
                }
            }
        }
    }

    public boolean hasAllSpecialPrice() {
        return bomSearchResponse.stream()
                .anyMatch(bomSearch -> bomSearch.salesOffers.stream().anyMatch(this::hasSpecialPrice));
    }

    public boolean hasSpecialPrice(SalesOffer offer) {
        double personalPrice = offer.getPersonalPrice();
        double marketPrice = offer.getPrice();
        return personalPrice > 0 && personalPrice < marketPrice;
    }

    public String renderSpecialPrice(SalesOffer offer) {
        return " — " + currencyFormat(offer.getPersonalPrice()) + " " + preferredCurrency.getSign();
    }

    private void updateOffersConverter(Collection<SalesOffersResponse> offers) {
        if (this.offersById == null) {
            this.offersById = new HashMap<>();
        }
        for (SalesOffersResponse offer : offers) {
            offersById.put(offer.getId(), offer);
        }
        Set<SalesOffer> salesOffers = offers.stream()
            .map(SalesOffersResponse::getSalesOffers)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
        if (salesOfferConverter == null) {
            salesOfferConverter = new ByIdConverter<>(salesOffers, SalesOffer::getSalesOfferId);
        } else {
            salesOfferConverter.addAll(salesOffers);
        }
    }

    private void combineSamePositions(List<CartPosition> positions) {
        Iterator<CartPosition> iterator = positions.iterator();
        Map<String, CartPosition> positionsByProductId = new HashMap<>();
        Map<String, CartPosition> positionsByDescription = new HashMap<>();
        logger.debug("Positions before updating: {0}", positions);
        while (iterator.hasNext()) {
            CartPosition next = iterator.next();
            String productId = next.getExternalProductId();
            if (productId != null) {
                updatePositions(iterator, positionsByProductId, next, productId);
            } else {
                updatePositions(iterator, positionsByDescription, next, next.getProductDescription());
            }
        }
        logger.debug("Positions after updating: {0}", positions);
    }

    private void updatePositions(Iterator<CartPosition> iterator,
                                 Map<String, CartPosition> existingPositionsMap,
                                 CartPosition next, String key) {
        CartPosition old = existingPositionsMap.get(key);
        if (old == null) {
            existingPositionsMap.put(key, next);
        } else {
            old.setCount(old.getCount() + next.getCount());
            iterator.remove();
        }
    }

    private List<BOMSearchResponse> filterNotExistingPositions(List<BOMSearchResponse> responses, Cart cart) {
        List<CartPosition> positions = cartPositionService.findForCart(cart);
        Set<String> productIds = positions.stream().map(CartPosition::getExternalProductId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Set<String> productDescriptions = positions.stream()
            .map(CartPosition::getProductDescription)
            .filter(Objects::nonNull)
            .filter(description -> !description.isBlank())
            .collect(Collectors.toSet());
        return responses.stream()
            .filter(resp -> resp.selectedProduct != null || !resp.queryRecord.toString().isBlank())
            .filter(resp -> resp.selectedProduct != null || !productDescriptions.contains(resp.queryRecord.toString()))
            .filter(resp -> resp.selectedProduct == null || !productIds.contains(resp.selectedProduct.getCode()))
            .collect(toList());
    }

    private Cart getUserCart() {
        long userId = LiferayPortletHelperUtil.getUserId();
        ExtendedOrganization contractor = LiferayUtil.findContractorForUser(userId);
        Cart currentCart = cartService.findCurrentForUser(userId);
        if (currentCart == null) {
            currentCart = cartService.createInitialCart(contractor, userId);
        }
        return currentCart;
    }

    public StreamedContent getCsvExample() {
        return queryParserService.getCsvExample();
    }

    public StreamedContent getXlsExample() {
        return queryParserService.getXlsExample();
    }

    public StreamedContent getXlsxExample() {
        return queryParserService.getXlsxExample();
    }

    public double getTotalPrice() {
        if (bomSearchResponse == null) {
            return 0.0;
        }
        return bomSearchResponse.stream()
            .mapToDouble(BOMSearchResponse::getTotal)
            .sum();
    }

    public Integer getMaxDays() {
        if (bomSearchResponse == null) {
            return null;
        }
        return bomSearchResponse.stream()
            .map(BOMSearchResponse::getSelectedOffer)
            .filter(Objects::nonNull)
            .map(SalesOffer::getDaysDuration)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder()).orElse(null);
    }

    public String convertStocksAmount(SalesOffer offer) {
        if (offer.getStockAmount() > 0) {
            return offer.getStockAmount() + "";
        }
        return "много";
    }

    public void resetOffer(BOMSearchResponse product) {
        product.selectedOffer = null;
        product.salesOffers = null;
        if (product.selectedProduct != null) {
            SalesOffersResponse salesOffersResponse = offersById
                .get(product.getSelectedProduct().getCode());
            if (salesOffersResponse == null) {
                salesOffersResponse = loadOffersForProduct(product.getSelectedProduct());
            }
            if (salesOffersResponse != null) {
                List<SalesOffer> salesOffers = salesOffersResponse
                    .getSalesOffers();

                product.salesOffers = salesOffers;
                if (salesOffers != null && !salesOffers.isEmpty()) {
                    product.selectedOffer = salesOffers.get(0);
                    product.updateAmount();
                }
            }
        }
    }

    private SalesOffersResponse loadOffersForProduct(ProductResponse.ProductHitSource product) {
        if (product == null) {
            logger.warn("Load offers for null product!");
            return null;
        }
        logger.debug("Load offers for product {0}", product.getCode());
        SalesOffersResponse offersResponse = OffersUtil.loadOffers(product.getProductId(), salesOffersService,
            preferredCurrency, selectedWarehouse);
        if (offersResponse == null) {
            logger.warn("Offers was not loaded!");
            LocalizedFacesMessage.error("itc.bom.can-not-fetch-offers");
        } else {
            updateOffersConverter(Collections.singleton(offersResponse));
        }
        return offersResponse;

    }

    public void addRow() {
        this.bomSearchResponse.add(new BOMSearchResponse(rowId, new QueryParserService.BOMQueryRecord(), null, null));
        ++rowId;
    }


    @Data
    @EqualsAndHashCode(of = {"index"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMSearchResponse implements Serializable {
        private int index;
        private QueryParserService.BOMQueryRecord queryRecord;
        private List<ProductResponse.ProductHitSource> response = new ArrayList<>();
        private ProductResponse.ProductHitSource selectedProduct;
        private List<SalesOffer> salesOffers = new ArrayList<>();
        private SalesOffer selectedOffer;
        private long amount;

        public BOMSearchResponse(int index, QueryParserService.BOMQueryRecord queryRecord,
                                 List<ProductResponse.ProductHitSource> response,
                                 ProductResponse.ProductHitSource selectedProduct) {
            this.index = index;
            this.queryRecord = queryRecord;
            this.response = response;
            this.selectedProduct = selectedProduct;
        }

        public long getStep() {
            return Optional.ofNullable(selectedOffer)
                .map(SalesOffer::getMultiplicity)
                .orElse(1).longValue();
        }

        public double getTotal() {
            if (selectedOffer == null) {
                return 0;
            }
            if (selectedOffer.getPrice() == null) {
                return 0;
            }
            return amount * selectedOffer.getPrice();
        }

        public Long getMaxCount() {
            if (selectedOffer == null) {
                return 0L;
            }
            return selectedOffer.getStockAmount() != 0 ? selectedOffer.getStockAmount() : Long.MAX_VALUE;
        }

        public Long getMinCount() {
            if (selectedOffer == null) {
                return 0L;
            }
            if (selectedOffer.getStockAmount() == 0) {
                return Long.valueOf(selectedOffer.getMinimalOrder());
            }
            return Long.valueOf(selectedOffer.getMinimalOrder() > selectedOffer.getStockAmount()
                ? selectedOffer.getStockAmount()
                : selectedOffer.getMinimalOrder());
        }

        public void updateAmount() {
            if (selectedOffer == null) {
                return;
            }
            if (amount > getMaxCount()) {
                amount = getMaxCount();
                return;
            }
            if (amount < getMinCount()) {
                amount = getMinCount();
                return;
            }
            if (amount % selectedOffer.getMultiplicity() != 0) {
                amount = amount - (amount % selectedOffer.getMultiplicity()) + selectedOffer.getMultiplicity();
            }
        }

        public void setSelectedOffer(SalesOffer selectedOffer) {
            this.selectedOffer = selectedOffer;
            updateAmount();
        }
    }


}
