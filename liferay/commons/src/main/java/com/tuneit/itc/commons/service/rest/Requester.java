package com.tuneit.itc.commons.service.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.commons.Pair;
import com.tuneit.itc.commons.model.Currency;
import com.tuneit.itc.commons.model.rest.CurrencyResponse;
import com.tuneit.itc.commons.model.rest.ManufacturerResponse;
import com.tuneit.itc.commons.model.rest.MarketPrice;
import com.tuneit.itc.commons.model.rest.OrderHistoryItem;
import com.tuneit.itc.commons.model.rest.OrderStatusItem;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.model.rest.ProductTypeResponse;
import com.tuneit.itc.commons.model.rest.SalesOffersResponse;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.util.ProductsUtil;

@Data
@ManagedBean
@ApplicationScoped
public class Requester {
    public static final Log log = LogFactoryUtil.getLog(Requester.class);
    private ProductsService productsService;
    private ManufacturerService manufacturerService;
    private ProductTypesService productTypesService;
    private OrderHistoryService orderHistoryService;
    private OrderStatusService orderStatusService;
    private MarketPricesService marketPricesService;
    private DocumentsDownloadService documentsDownloadService;
    private CurrencyService currencyService;
    private WarehouseService warehousesService;
    private SalesOffersService salesOffersService;
    private PlaceOrderService placeOrderService;
    private CheckDocumentsService checkDocumentsService;

    public String getUrlBase() {
        return RequesterProperties.getBaseUrl();
    }

    @PostConstruct
    public void init() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(new RequestInterceptor()).build();
        ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        Retrofit build = new Retrofit.Builder()
            .baseUrl(getUrlBase())
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build();
        productsService = build.create(ProductsService.class);
        manufacturerService = build.create(ManufacturerService.class);
        productTypesService = build.create(ProductTypesService.class);
        orderHistoryService = build.create(OrderHistoryService.class);
        orderStatusService = build.create(OrderStatusService.class);
        marketPricesService = build.create(MarketPricesService.class);
        documentsDownloadService = build.create(DocumentsDownloadService.class);
        currencyService = build.create(CurrencyService.class);
        warehousesService = build.create(WarehouseService.class);
        salesOffersService = build.create(SalesOffersService.class);
        placeOrderService = build.create(PlaceOrderService.class);
        checkDocumentsService = build.create(CheckDocumentsService.class);
    }

    public interface ProductsService {
        String API_V_2_PRODUCTS = "/api/v2/products";
        String ID_PARAM = "Id";
        String VENDOR_CODE_PARAM = "PartNumber";
        String MANUFACTURER_PARAM = "Manufacturer";
        String QUERY_PARAM = "Query";
        String SIZE_PARAM = "Size";
        String FROM_PARAM = "From";
        String PRODUCT_TYPE_ID = "ProductTypeID";
        String MANUFACTURER_ID = "ManufacturerID";
        String FEATURE_PARAM = "Features";
        String REPLACEMENT_PARAM = "Replacement";

        String USERNAME_PARAM = "UserName";
        String USEREXT_PARAM = "UserExt";
        String USEREXT_PARTNER_ID = "PartnerID";
        String USEREXT_IP_PARAM = "IP";
        String USEREXT_WAREHOUSE_PARAM = "Warehouse";
        String USEREXT_SESSION_ID = "SessionID";
        String USEREXT_GEOPOINT_PARAM = "Geopoint";

        @GET(API_V_2_PRODUCTS + "?Features=True")
        Call<ProductResponse> getById(@Query(ID_PARAM) String id, @Query(REPLACEMENT_PARAM) boolean replacement);

        default Call<ProductResponse> getById(String id) {
            return getById(id, false);
        }

        @POST(API_V_2_PRODUCTS)
        Call<List<ProductResponse>> getAllByIds(@Body List<IdParam> ids);

        @POST(API_V_2_PRODUCTS)
        Call<List<ProductResponse>> getAllByIdsWithFeatures(@Body List<IdAndFeaturesParam> ids);

        default Call<List<ProductResponse>> getByIds(List<String> ids) {
            List<IdParam> params = ids.stream()
                .map(IdParam::new)
                .collect(Collectors.toList());
            return getAllByIds(params);
        }

        default Call<List<ProductResponse>> getByIdsWithFeatures(List<String> ids) {
            List<IdAndFeaturesParam> params = ids.stream()
                .map(s -> new IdAndFeaturesParam(s, true))
                .collect(Collectors.toList());
            return getAllByIdsWithFeatures(params);
        }


        default Call<List<ProductResponse>> getByVendorCode(@Query(VENDOR_CODE_PARAM) String partNumber,
                                                            @Query(FROM_PARAM) long from,
                                                            @Query(SIZE_PARAM) long size) {
            ProductRequestBody req = new ProductRequestBody(from, size);
            req.setPartNumber(partNumber);
            return doRequest(req);
        }

        default Call<List<ProductResponse>> getByVendorCodeAndProductTypeAndManufacturer(String partNumber,
                                                                           long from,
                                                                           long size,
                                                                           String prodId,
                                                                           String manId) {
            ProductRequestBody req = new ProductRequestBody(from, size, prodId, manId);
            req.setPartNumber(partNumber);
            return doRequest(req);
        }


        default Call<List<ProductResponse>> getByVendorCodeAndManufacturer(@Query(VENDOR_CODE_PARAM) String partNumber,
                                                             @Query(MANUFACTURER_PARAM) String manufacturer) {
            return doRequest(new ProductRequestBody(partNumber, manufacturer));
        }

        default Call<List<ProductResponse>> query(@Query(QUERY_PARAM) String query,
                                                  @Query(FROM_PARAM) long from,
                                                  @Query(SIZE_PARAM) long size) {
            return doRequest(new ProductRequestBody(query, from, size));
        }

        default Call<List<ProductResponse>> queryAndProductTypeAndManufacturer(String query,
                                                                 long from,
                                                                 long size,
                                                                 String prodId,
                                                                 String manId) {
            ProductRequestBody req = new ProductRequestBody(from, size, prodId, manId);
            req.setQuery(query);
            return doRequest(req);
        }

        default Call<List<ProductResponse>> getByProductType(@Query(PRODUCT_TYPE_ID) String productType,
                                               @Query(FROM_PARAM) long from,
                                               @Query(SIZE_PARAM) long size) {
            ProductRequestBody req = new ProductRequestBody(from, size);
            req.setProdId(productType);
            return doRequest(req);
        }

        @GET(API_V_2_PRODUCTS)
        Call<ProductResponse> getPage(@Query(FROM_PARAM) long from, @Query(SIZE_PARAM) long size);

        default Call<List<ProductResponse>> queryManufacturer(@Query(QUERY_PARAM) String query,
                                                @Query(MANUFACTURER_PARAM) String manufacturer,
                                                @Query(FROM_PARAM) long from,
                                                @Query(SIZE_PARAM) long size) {
            return doRequest(new ProductRequestBody(query, manufacturer, from, size));
        }

        @POST(API_V_2_PRODUCTS)
        Call<List<ProductResponse>> bomQuery(@Body List<BOMQuery> queries);

        @GET(API_V_2_PRODUCTS)
        Call<ProductResponse> bomQuery(@QueryMap Map<String, String> params);

        default Call<ProductResponse> bomQuery(String query, String manufacturer, String article) {
            Map<String, String> queryParams = new HashMap<>();
            if (article != null && !article.isBlank()) {
                queryParams.put(VENDOR_CODE_PARAM, article);
            }
            if (manufacturer != null && !manufacturer.isBlank()) {
                queryParams.put(MANUFACTURER_PARAM, manufacturer);
            }
            if (article == null || article.isBlank()) {
                if (query != null && !query.isBlank()) {
                    queryParams.put(QUERY_PARAM, query);
                }
            }
            return bomQuery(queryParams);
        }

        default Call<List<ProductResponse>> doRequest(ProductRequestBody req) {
            return doRequest(Collections.singletonList(req));
        }

        @POST(API_V_2_PRODUCTS)
        Call<List<ProductResponse>> doRequest(@Body List<ProductRequestBody> req);

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        class IdParam {
            @JsonProperty(ID_PARAM)
            private String id;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        class BOMQuery {
            @JsonProperty(MANUFACTURER_PARAM)
            private String manufacturer;
            @JsonProperty(VENDOR_CODE_PARAM)
            private String article;
            @JsonProperty(QUERY_PARAM)
            private String query;
            @JsonProperty(SIZE_PARAM)
            private long size;

            @JsonProperty(USERNAME_PARAM)
            private String userName;
            @JsonProperty(USEREXT_PARAM)
            private UserExt userExt;

            public BOMQuery(String manufacturer, String article, String query, long size) {
                if ((manufacturer != null && !manufacturer.isBlank()) && (article != null && !article.isBlank())
                    && (query != null && !query.isBlank())) {
                    query = null;
                }
                this.manufacturer = manufacturer;
                this.article = article;
                this.query = query;
                this.size = size;

                this.userName = ProductsUtil.getUserName();
                this.userExt = ProductsUtil.getUserExt();
            }

            public BOMQuery(String manufacturer, String article, String query) {
                this(manufacturer, article, query, 5L);
            }
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        class IdAndFeaturesParam {
            @JsonProperty(ID_PARAM)
            private String id;
            @JsonProperty(FEATURE_PARAM)
            private boolean feature;
        }

        @Data
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class UserExt {
            @JsonProperty(USEREXT_PARTNER_ID)
            private String partnerId;
            @JsonProperty(USEREXT_IP_PARAM)
            private String ip;
            @JsonProperty(USEREXT_WAREHOUSE_PARAM)
            private String warehouse;
            @JsonProperty(USEREXT_SESSION_ID)
            private String sessionId;
            @JsonProperty(USEREXT_GEOPOINT_PARAM)
            String geopoint;
        }

        @Data
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class ProductRequestBody {
            @JsonProperty(ID_PARAM)
            private String id;
            @JsonProperty(VENDOR_CODE_PARAM)
            private String partNumber;
            @JsonProperty(MANUFACTURER_PARAM)
            private String manufacturer;
            @JsonProperty(QUERY_PARAM)
            private String query;
            @JsonProperty(SIZE_PARAM)
            private Long size;
            @JsonProperty(FROM_PARAM)
            private Long from;
            @JsonProperty(PRODUCT_TYPE_ID)
            private String prodId;
            @JsonProperty(MANUFACTURER_ID)
            private String manId;
            @JsonProperty(FEATURE_PARAM)
            private Boolean feature;
            @JsonProperty(REPLACEMENT_PARAM)
            private Boolean replacement;

            @JsonProperty(USERNAME_PARAM)
            private String userName;
            @JsonProperty(USEREXT_PARAM)
            private UserExt userExt;

            public ProductRequestBody() {
                this.userName = ProductsUtil.getUserName();
                this.userExt = ProductsUtil.getUserExt();
            }

            public ProductRequestBody(long from, long size, String prodId, String manId) {
                this();

                this.from = from;
                this.size = size;
                this.prodId = prodId;
                this.manId = manId;
            }

            public ProductRequestBody(String query, long from, long size) {
                this();

                this.query = query;
                this.from = from;
                this.size = size;
            }

            public ProductRequestBody(long from, long size) {
                this();

                this.from = from;
                this.size = size;
            }

            public ProductRequestBody(String partNumber, String manufacturer) {
                this();

                this.partNumber = partNumber;
                this.manufacturer = manufacturer;
            }

            public ProductRequestBody(String query, String manufacturer, long from, long size) {
                this();

                this.query = query;
                this.manufacturer = manufacturer;
                this.from = from;
                this.size = size;
            }
        }
    }

    public interface ManufacturerService {

        String API_V_2_MANUFACTURERS = "/api/v2/manufacturers";
        String ID_PARAM = "Id";
        String QUERY_PARAM = "Query";
        String SIZE_PARAM = "Size";
        String FROM_PARAM = "From";

        @GET(API_V_2_MANUFACTURERS + "?AllPosition=False")
        Call<ManufacturerResponse> getById(@Query(ID_PARAM) String id);

        @GET(API_V_2_MANUFACTURERS + "?AllPosition=False")
        Call<ManufacturerResponse> query(@Query(QUERY_PARAM) String query, @Query(FROM_PARAM) long from,
                                         @Query(SIZE_PARAM) long size);

        @GET(API_V_2_MANUFACTURERS + "?AllPosition=False")
        Call<ManufacturerResponse> page(@Query(FROM_PARAM) long from, @Query(SIZE_PARAM) long size);
    }

    public interface ProductTypesService {

        String API_V_2_PRODUCT_TYPES = "/api/v2/productTypes";
        String ID_PARAM = "Id";
        String QUERY_PARAM = "Query";
        String SIZE_PARAM = "Size";
        String FROM_PARAM = "From";
        String PARENT_ID = "ParentID";

        @GET(API_V_2_PRODUCT_TYPES)
        Call<ProductTypeResponse> getById(@Query(ID_PARAM) String id);

        @GET(API_V_2_PRODUCT_TYPES)
        Call<ProductTypeResponse> query(@Query(QUERY_PARAM) String query, @Query(FROM_PARAM) long from,
                                        @Query(SIZE_PARAM) long size);

        @GET(API_V_2_PRODUCT_TYPES)
        Call<ProductTypeResponse> query(@Query(QUERY_PARAM) String query);

        @GET(API_V_2_PRODUCT_TYPES)
        Call<ProductTypeResponse> getPage(@Query(FROM_PARAM) long from, @Query(SIZE_PARAM) long size);

        @GET(API_V_2_PRODUCT_TYPES + "?Hierarchy=Folders")
        Call<ProductTypeResponse> getGroupPage(@Query(FROM_PARAM) long from, @Query(SIZE_PARAM) long size);

        @GET(API_V_2_PRODUCT_TYPES + "?Hierarchy=Folders")
        Call<ProductTypeResponse> getGroupPageWithQuery(@Query(QUERY_PARAM) String query, @Query(FROM_PARAM) long from,
                                                        @Query(SIZE_PARAM) long size);

        @GET(API_V_2_PRODUCT_TYPES + "?Hierarchy=Items")
        Call<ProductTypeResponse> getByParent(@Query("ParentID") String parentId, @Query(FROM_PARAM) long from,
                                              @Query(SIZE_PARAM) long size);

        @GET(API_V_2_PRODUCT_TYPES + "?Hierarchy=Items")
        Call<ProductTypeResponse> getByParentWithQuery(@Query(QUERY_PARAM) String query,
                                                       @Query("ParentID") String parentId,
                                                       @Query(FROM_PARAM) long from,
                                                       @Query(SIZE_PARAM) long size);

        @GET(API_V_2_PRODUCT_TYPES + "?WithSortOrder=true")
        Call<ProductTypeResponse> getPageWithSortingOrder(@Query(FROM_PARAM) long from, @Query(SIZE_PARAM) long size);

    }

    public interface OrderHistoryService {

        String API_V_2_ORDER_HISTORY = "/api/v2/documents/ordersHistory";
        String ID_PARAM = "Id";
        String START_DATE_PARAM = "StartDate";
        String END_DATE_PARAM = "EndDate";

        @GET(API_V_2_ORDER_HISTORY)
        Call<List<OrderHistoryItem>> getById(@Query(ID_PARAM) String id);

        @GET(API_V_2_ORDER_HISTORY)
        Call<List<OrderHistoryItem>> getByIdInInterval(@Query(ID_PARAM) String id,
                                                       @Query(START_DATE_PARAM) OrderHistoryItem.DateWrapper from,
                                                       @Query(END_DATE_PARAM) OrderHistoryItem.DateWrapper to);

        @GET(API_V_2_ORDER_HISTORY)
        Call<List<OrderHistoryItem>> getByIdInInterval(@Query(ID_PARAM) String id,
                                                       @Query(START_DATE_PARAM) OrderHistoryItem.DateWrapper from);

    }

    public interface OrderStatusService {
        String API_V_2_ORDER_HISTORY = "/api/v2/documents/ordersStatuses";
        String ID_PARAM = "Id";

        @GET(API_V_2_ORDER_HISTORY)
        Call<List<OrderStatusItem>> getById(@Query(ID_PARAM) String id);
    }

    public interface DocumentsDownloadService {
        String API_V_2_DOWNLOAD_BILL = "/api/v2/documents/download/schet";
        String API_V_2_DOWNLOAD_INVOICE = "/api/v2/documents/download/schetFactura";
        String ID_PARAM = "Id";

        @GET(API_V_2_DOWNLOAD_BILL)
        Call<ResponseBody> getBillById(@Query(ID_PARAM) String id);

        @GET(API_V_2_DOWNLOAD_INVOICE)
        Call<ResponseBody> getInvoiceById(@Query(ID_PARAM) String id);
    }

    public interface CheckDocumentsService {
        String API_V_2_CHECK_BILL = "/api/v2/documents/checkDownload/schet";
        String API_V_2_CHECK_INVOICE = "/api/v2/documents/checkDownload/schetFactura";
        String ID_PARAM = "Id";

        @GET(API_V_2_CHECK_BILL)
        Call<ResponseBody> checkBillByIdRequest(@Query(ID_PARAM) String id);

        @GET(API_V_2_CHECK_INVOICE)
        Call<ResponseBody> checkInvoiceByIdRequest(@Query(ID_PARAM) String id);

        default boolean checkInvoiceById(String id) throws IOException {
            return checkInvoiceByIdRequest(id)
                .execute()
                .isSuccessful();
        }

        default boolean checkBillById(String id) throws IOException {
            return checkBillByIdRequest(id)
                .execute()
                .isSuccessful();
        }
    }

    public interface MarketPricesService {
        String API_V_2_MARKET_PRICES = "/api/v2/pricing/marketPrices";
        String ID_PARAM = "Id";
        String CURRENCY_PARAM = "CurrencyID";
        String CUSTOMER_ID = "CustomerId";

        @POST(API_V_2_MARKET_PRICES)
        Call<List<MarketPrice>> getByIdParams(@Body List<PriceQuery> ids);

        default Call<List<MarketPrice>> getByIds(List<String> ids, String currencyId, String customerId) {
            List<PriceQuery> idParams = ids.stream()
                .map(id -> new PriceQuery(id, currencyId, customerId))
                .collect(Collectors.toList());
            return getByIdParams(idParams);
        }

        default Call<List<MarketPrice>> getByIds(List<String> ids, Currency currency, String customerId) {
            return getByIds(ids, currency == null ? "" : currency.getId(), customerId);
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class PriceQuery {
            @JsonProperty(ID_PARAM)
            private String productId;
            @JsonProperty(CURRENCY_PARAM)
            private String currencyId;
            @JsonProperty(CUSTOMER_ID)
            private String customerId;
        }
    }

    public interface WarehouseService {
        String API_V_2_WAREHOUSES = "/api/v2/pricing/warehouses";

        @GET(API_V_2_WAREHOUSES)
        Call<List<Warehouse>> getAll();
    }

    public interface CurrencyService {
        String API_V_2_CURRENCIES = "/api/v2/currencies";

        @GET(API_V_2_CURRENCIES)
        Call<CurrencyResponse> getAll();
    }

    public interface SalesOffersService {
        String API_V_2_SALES_OFFERS = "/api/v2/pricing/salesOffers";

        String PRODUCT_ID = "Id";
        String WAREHOUSE_ID = "WarehouseId";
        String CURRENCY_ID = "CurrencyId";
        String CUSTOMER_ID = "CustomerId";

        @GET(API_V_2_SALES_OFFERS)
        Call<List<SalesOffersResponse>> get(@Query(PRODUCT_ID) String productId,
                                            @Query(WAREHOUSE_ID) String warehouseId,
                                            @Query(CURRENCY_ID) String currencyId);

        @POST(API_V_2_SALES_OFFERS)
        Call<List<SalesOffersResponse>> get(@Body List<SalesOfferQuery> queries);

        default Call<List<SalesOffersResponse>> get(List<String> productIds, String currencyId, String warehouseId) {
            List<SalesOfferQuery> query = productIds.stream()
                    .map(id -> new SalesOfferQuery(null, id, warehouseId, currencyId))
                    .collect(Collectors.toList());
            return get(query);
        }

        @GET(API_V_2_SALES_OFFERS)
        Call<List<SalesOffersResponse>> getWithUserData(@Query(PRODUCT_ID) String productId,
                                                        @Query(WAREHOUSE_ID) String warehouseId,
                                                        @Query(CURRENCY_ID) String currencyId,
                                                        @Query(CUSTOMER_ID) String customerId);

        default Call<List<SalesOffersResponse>> getWithSelected(List<Pair<String, String>> productIds,
                                                                String warehouseId, String currencyId) {
            List<SalesOfferQuery> query = productIds.stream()
                .map(id -> new SalesOfferQuery(id.getFirst(), id.getSecond(), warehouseId, currencyId))
                .collect(Collectors.toList());
            return get(query);
        }

        default Call<List<SalesOffersResponse>> getWithSelectedAndUserData(List<Pair<String, String>> productIds,
                                                                           String warehouseId, String currencyId,
                                                                           String customerId) {
            List<SalesOfferQuery> query = productIds.stream()
                    .map(id -> new SalesOfferQuery(id.getFirst(), id.getSecond(), warehouseId, currencyId, customerId))
                    .collect(Collectors.toList());
            return get(query);
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class SalesOfferQuery {
            @JsonProperty("QueryId")
            private String queryId;
            @JsonProperty(PRODUCT_ID)
            private String productId;
            @JsonProperty(WAREHOUSE_ID)
            private String warehouseId;
            @JsonProperty(CURRENCY_ID)
            private String currencyId;
            @JsonProperty(CUSTOMER_ID)
            private String customerId;

            SalesOfferQuery(String queryId, String productId, String warehouseId, String currencyId) {
                this(queryId, productId, warehouseId, currencyId, null);
            }
        }
    }

    public interface PlaceOrderService {
        String API_V_2_PLACE_ORDER = "/api/v2/documents/orders";

        String CUSTOMER_ID = "IdCustomer"; //backOfficeCode контрагента
        String USER = "User"; // Фио пользователя
        String CART_NUMBER = "NumberCart"; //Код корзины
        String COMMENTS = "Comments"; //Комментарий пользователя
        String SUPPLY = "Supply";
        String SUPPLY_OPTION_ID = "IdSupplyOption"; //Id варианта поставки
        String PRODUCT_ID = "Id"; //Код товара
        String COUNT = "Count"; //К-во товара
        String ORDER_ID = "Id";
        String ORDER_DATE = "Date";
        String ORDER_NUMBER = "Number";
        String I_WANT_A_DISCOUNT = "IWantADiscount";
        String ATTACHMENTS = "Attachments";
        String I_WANT_A_DISCOUNT_FILE = "IWantADiscountFile";
        String I_WANT_A_DISCOUNT_FILE_EXTENSION = "IWantADiscountFileExtension";

        @POST(API_V_2_PLACE_ORDER)
        Call<List<PlaceOrderResponse>> placeOrder(@Body PlaceOrderRequest request);

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class PlaceOrderRequest {
            @JsonProperty(CUSTOMER_ID)
            private String contractorId;
            @JsonProperty(USER)
            private String userFio;
            @JsonProperty(CART_NUMBER)
            private String cartNumber;
            @JsonProperty(COMMENTS)
            private String comments;
            @JsonProperty(I_WANT_A_DISCOUNT)
            private String wantDiscount;
            @JsonProperty(ATTACHMENTS)
            private List<AttachmentFile> attachments;
            @JsonProperty(SUPPLY)
            private List<SupplyOrder> supply;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class AttachmentFile {
            @JsonProperty(I_WANT_A_DISCOUNT_FILE)
            private String file;
            @JsonProperty(I_WANT_A_DISCOUNT_FILE_EXTENSION)
            private String extension;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class SupplyOrder {
            @JsonProperty(SUPPLY_OPTION_ID)
            private String salesOfferId;
            @JsonProperty(PRODUCT_ID)
            private String productId;
            @JsonProperty(COUNT)
            private Long count;
        }

        @Data
        class PlaceOrderResponse {
            @JsonProperty(ORDER_ID)
            private String orderId;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
            @JsonProperty(ORDER_DATE)
            private Date orderDate;
            @JsonProperty(ORDER_NUMBER)
            private String orderNumber;
        }
    }

}

