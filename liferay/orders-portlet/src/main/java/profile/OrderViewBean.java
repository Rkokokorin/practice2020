package profile;

import lombok.Data;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.util.StringUtil;

import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.rest.OrderHistoryItem;
import com.tuneit.itc.commons.model.rest.OrderStatusItem;
import com.tuneit.itc.commons.model.rest.ProductResponse;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.HttpUtil;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.commons.util.WindowsPathNormalizer;

/**
 * Orders view.
 *
 * @author Alexander Pashnin
 */
@Data
@ViewScoped
@ManagedBean
public class OrderViewBean {
    private String orderId;
    private OrderHistoryItem selectedOrder = new OrderHistoryItem();
    private List<OrderStatusItem> statusItems = new ArrayList<>();
    private List<OrderStatusItem> allHistory = new ArrayList<>();
    private Map<String, String> pictures = new HashMap<>();

    private String catalogueBaseUrl;
    private String productIdParamName = ParamNameConstants.PRODUCT;

    private byte[] billBytes;
    private boolean billExists;
    private String billContentType;
    private String billFilename;
    private byte[] invoiceBytes;
    private boolean invoiceExists;
    private String invoiceContentType;
    private String invoiceFilename;
    private String pdfSvgIcon;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ManagedProperty("#{requester.orderStatusService}")
    private Requester.OrderStatusService orderStatusService;
    @ManagedProperty("#{requester.orderHistoryService}")
    private Requester.OrderHistoryService orderHistoryService;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{requester.documentsDownloadService}")
    private Requester.DocumentsDownloadService documentsDownloadService;
    @ManagedProperty("#{requester.checkDocumentsService}")
    private Requester.CheckDocumentsService checkDocumentsService;

    @ManagedProperty("#{ordersBean}")
    private OrdersBean ordersBean;

    @PostConstruct
    public void init() {
        orderId = HttpUtil.getRequestParam(ParamNameConstants.ORDER);
        try {
            LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
            catalogueBaseUrl = catalogueUrl.toString();
            catalogueBaseUrl = catalogueBaseUrl.substring(0, catalogueUrl.toString().indexOf('?'));
        } catch (PortalException e) {
            log.error("Can't get catalogue base url. ", e);
            return;
        }
        loadOrder();
        loadStatuses();
        loadImages();
        checkBill();
        checkInvoice();

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            var pdf = ec.getResourceAsStream("/WEB-INF/resources/images/pdf.svg");
            pdfSvgIcon = StringUtil.read(pdf);
        } catch (Exception exc) {
            log.error("Error while svg initialization!", exc);
        }
    }


    public List<OrderHistoryItem> getOrderInfo() {
        List<OrderHistoryItem> orderHistoryItems = new ArrayList<>();
        orderHistoryItems.add(selectedOrder);
        return orderHistoryItems;
    }

    public boolean filterByDate(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim();
        if (filterText == null || filterText.equals("")) {
            return true;
        }
        if (value == null) {
            return false;
        }
        return new SimpleDateFormat("dd.MM.yyyy").format((Date) value)
            .contains(filterText);
    }

    private void checkBill() {
        if (selectedOrder != null) {
            try {
                billExists = checkDocumentsService.checkBillById(selectedOrder.getId());
            } catch (IOException ioe) {
                billExists = false;
                log.error("Error while check bill: {0}", ioe.getMessage());
            }
        }
    }

    private void checkInvoice() {
        if (selectedOrder != null) {
            try {
                invoiceExists = checkDocumentsService.checkInvoiceById(selectedOrder.getId());
            } catch (IOException ioe) {
                invoiceExists = false;
                log.error("Error while check invoice: {0}", ioe.getMessage());
            }
        }
    }

    private void loadOrder() {
        if (ordersBean.getOrders() == null) {
            log.error("Can't find order with id {0} for contractor {1}", orderId, ordersBean.getContractorId());
            selectedOrder = null;
        } else {
            selectedOrder = ordersBean.getOrders()
                .stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst()
                .orElse(null);
        }
    }

    private void loadBill() {
        if (selectedOrder != null) {
            try {
                Response<ResponseBody> response = documentsDownloadService.getBillById(orderId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String filename;
                    String filenameHeader = response
                        .headers()
                        .values("content-disposition")
                        .stream()
                        .filter(s -> s.contains("filename"))
                        .findFirst()
                        .orElse(null);
                    if (filenameHeader == null) {
                        filename = "Счёт_" + selectedOrder.getNumber() + "_от_" + selectedOrder.getDate() + ".pdf";
                    } else {
                        filename = URLDecoder.decode(filenameHeader.substring(filenameHeader.indexOf('"') + 1,
                            filenameHeader.lastIndexOf('"')), StandardCharsets.UTF_8);
                    }
                    billBytes = response.body().bytes();
                    billContentType = response.body().contentType().toString();
                    billFilename = filename;
                } else {
                    if (response.code() != 404) {
                        log.error("Can't download bill for order {0}. Response is {1}", orderId, response);
                    }
                }
            } catch (IOException e) {
                log.error("Can't download bill for order {0}", orderId);
                log.error(e);
            }
        }
    }

    private void loadInvoice() {
        if (selectedOrder != null) {
            try {
                Response<ResponseBody> response = documentsDownloadService.getInvoiceById(orderId).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String filename;
                    String filenameHeader = response
                        .headers()
                        .values("content-disposition")
                        .stream()
                        .filter(s -> s.contains("filename"))
                        .findFirst()
                        .orElse(null);
                    if (filenameHeader == null) {
                        filename = "СчётФактура_" + selectedOrder.getNumber() + "_от_" + selectedOrder.getDate()
                            + ".pdf";
                    } else {
                        filename = URLDecoder.decode(filenameHeader.substring(filenameHeader.indexOf('"') + 1,
                            filenameHeader.lastIndexOf('"')), StandardCharsets.UTF_8);
                    }
                    invoiceBytes = response.body().bytes();
                    invoiceContentType = response.body().contentType().toString();
                    invoiceFilename = filename;
                } else {
                    if (response.code() != 404) {
                        log.error("Can't download invoice for order {0}. Response is {1}", orderId, response);
                    }
                }
            } catch (IOException e) {
                log.error("Can't download invoice for order {0}", orderId);
                log.error(e);
            }
        }
    }

    private void loadImages() {
        if (!statusItems.isEmpty()) {
            try {
                Response<List<ProductResponse>> response = productsService.getAllByIds(statusItems
                    .stream()
                    .filter(item -> item.getProductCode() != null && !item.getProductCode().isBlank())
                    .map(item -> new Requester.ProductsService.IdParam(item.getProductCode()))
                    .collect(Collectors.toList())).execute();
                List<ProductResponse> body = response.body();
                if (!response.isSuccessful() || body == null) {
                    log.error("Can't fetch products for list of ids. Response is: {0}", response);
                    return;
                }
                pictures = body
                    .stream()
                    .map(pr -> pr.getHits().getHits())
                    .filter(l -> !l.isEmpty())
                    .map(l -> l.get(0).getSource())
                    .collect(Collectors.toMap(
                        ProductResponse.ProductHitSource::getCode, WindowsPathNormalizer::getProductImageUrl)
                    );
            } catch (IOException e) {
                log.error("Can't fetch products for list of ids");
                log.error(e);
            }
        }
    }

    private void loadStatuses() {
        if (selectedOrder != null) {
            try {
                Response<List<OrderStatusItem>> response = orderStatusService.getById(orderId).execute();
                List<OrderStatusItem> body = response.body();
                if (!response.isSuccessful() || body == null) {
                    log.error("Can't fetch order status for order {0}. Response: {1}", orderId, response);
                    return;
                }
                allHistory = body;
                statusItems = body
                    .stream()
                    .collect(Collectors.groupingBy(OrderStatusItem::getProductName,
                        Collectors.maxBy(Comparator.comparing(OrderStatusItem::getDate))))
                    .values()
                    .stream()
                    .map(Optional::get)
                    .collect(Collectors.toList());
            } catch (IOException e) {
                log.error("Can't fetch order status for order {0}.", orderId);
                log.error(e);
            }
        }
    }


    public boolean productHasCode(OrderStatusItem product) {
        return product.getProductCode() != null && !product.getProductCode().isBlank();
    }

    public String productPictureUrl(OrderStatusItem product) {
        return pictures.getOrDefault(product.getProductCode(), WindowsPathNormalizer.getDefaultProductImageUrl());
    }

    public StreamedContent getBillPdf() {
        loadBill();
        return new DefaultStreamedContent(new ByteArrayInputStream(billBytes), billContentType, billFilename);
    }

    public StreamedContent getInvoicePdf() {
        loadInvoice();
        return new DefaultStreamedContent(new ByteArrayInputStream(invoiceBytes), invoiceContentType, invoiceFilename);
    }
}
