package com.tuneit.itc.cart;

import lombok.Data;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.StringUtil;

import com.tuneit.itc.cart.model.CartRow;
import com.tuneit.itc.commons.PortletIdConstants;
import com.tuneit.itc.commons.RoleConstants;
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
import com.tuneit.itc.commons.model.rest.SalesOffer;
import com.tuneit.itc.commons.model.rest.SalesOffersResponse;
import com.tuneit.itc.commons.model.rest.Warehouse;
import com.tuneit.itc.commons.service.CurrencyService;
import com.tuneit.itc.commons.service.WarehouseService;
import com.tuneit.itc.commons.service.cart.CartPositionService;
import com.tuneit.itc.commons.service.cart.CartService;
import com.tuneit.itc.commons.service.rest.Requester;
import com.tuneit.itc.commons.util.CartUtil;
import com.tuneit.itc.commons.util.LiferayUtil;
import com.tuneit.itc.commons.util.OffersUtil;


/**
 * Utility bean for loading cart, its content and managing it.
 *
 * @author Alexander Pashnin
 */
@Data
@ViewScoped
@ManagedBean
public class CartLoaderBean implements Serializable {

    Logger log = LoggerFactory.getLogger(CartLoaderBean.class);
    private User user;
    private String cartId;
    private List<CartRow> orders = new ArrayList<>();
    private List<CartRow> filteredOrders = new ArrayList<>();
    private Cart currentCart;
    private List<CartPosition> order;
    private Cart lastCart;
    private List<CartRow> lastOrders;
    private boolean viewAccess;
    private String orderComment = "";
    private boolean wantDiscount;
    private String discountComment = "";
    private List<UploadedFile> uploadedFiles = Collections.synchronizedList(new ArrayList<>());
    private CartRow lastRemovedItem;
    @ManagedProperty("#{liferay}")
    private Liferay liferay;
    @ManagedProperty("#{cartService}")
    private CartService cartService;
    @ManagedProperty("#{cartPositionService}")
    private CartPositionService cartPositionService;
    @ManagedProperty("#{requester.productsService}")
    private Requester.ProductsService productsService;
    @ManagedProperty("#{warehouseService}")
    private WarehouseService warehouseService;
    @ManagedProperty("#{currencyService}")
    private CurrencyService currencyService;
    @ManagedProperty("#{requester.salesOffersService}")
    private Requester.SalesOffersService salesOffersService;
    @ManagedProperty("#{requester.placeOrderService}")
    private Requester.PlaceOrderService placeOrderService;
    private boolean cartUpdateStatus = false;
    private ByIdConverter<SalesOffer> offerConverter;
    private Map<String, ProductResponse.ProductHitSource> productsByExternalId;

    private List<String> daysWords = new ArrayList<>(Arrays.asList("день", "дня", "дней"));
    private boolean actionNeeded;
    private boolean lockAjaxPolling;
    private boolean savingCompleted;
    private List<Requester.PlaceOrderService.PlaceOrderResponse> createdOrders;

    private Currency preferredCurrency;
    private Warehouse selectedWarehouse;


    @SneakyThrows
    @PostConstruct
    public void init() {
        preferredCurrency = currencyService.getUserCurrency();
        selectedWarehouse = warehouseService.getUserWarehouse();
        lockAjaxPolling = false;

        user = liferay.getThemeDisplay().getUser();

    }

    public void updateCurrency() {
        this.preferredCurrency = currencyService.getUserCurrency();
        this.selectedWarehouse = warehouseService.getUserWarehouse();
        loadOffers();
    }

    public void loadCart() {
        var currentCart = cartService.findCurrentForUser(user.getUserId());
        loadCart(currentCart);
    }

    public void loadCart(Long cartId) {
        var currentCart = cartService.find(cartId).orElse(null);
        loadCart(currentCart);
    }

    private void loadCart(Cart cart) {
        currentCart = cart;
        if (currentCart != null) {
            cartId = currentCart.getContractorCode() + "-" + currentCart.getContractorLocalCartId();
        }
        viewAccess = hasViewAccess();
        loadOrders();
        lastCart = currentCart;
    }

    public void reloadCart() {
        if (lockAjaxPolling) {
            return;
        }
        cartUpdateStatus = false;
        var currentCart = cartService.findCurrentForUser(user.getUserId());
        var order = cartPositionService.findForCart(currentCart);
        if (null == currentCart) {
            if (null != this.currentCart) {
                cartUpdateStatus = true;
            }
            this.currentCart = currentCart;
            viewAccess = hasViewAccess();
            loadOrders();
            return;
        }

        if (this.order == null && order != null) {
            cartUpdateStatus = true;
        } else if (this.order != null && order == null) {
            cartUpdateStatus = true;
        } else if (this.currentCart == null) {
            cartUpdateStatus = true;
        } else if (!currentCart.getId().equals(this.currentCart.getId())) {
            cartUpdateStatus = true;
        } else if (!Objects.equals(this.order, order)) {
            cartUpdateStatus = true;
        }

        if (cartUpdateStatus) {
            this.currentCart = currentCart;
            this.cartId = currentCart.getContractorCode() + "-" + currentCart.getContractorLocalCartId();
            this.viewAccess = hasViewAccess();
            loadOrders();
        }
    }

    public void upload(FileUploadEvent event) {
        if (event.getFile().getSize() > 10 * 1024 * 1024) {
            return;
        }

        uploadedFiles.add(event.getFile());
    }

    @SneakyThrows
    public String redirectUrl(CartRow order) {
        LiferayPortletURL url = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
        url.setParameter(ParamNameConstants.PRODUCT, order.getPcode());
        return url.toString();
    }

    private void loadOrders() {
        if (!viewAccess) {
            return;
        }
        if (currentCart == null) {
            order = new ArrayList<>();
        } else {
            order = cartPositionService.findForCart(currentCart);
            productsByExternalId = CartUtil.fromCartPositions(productsService, order);
            orders = order.stream()
                .map(position -> CartRow.fromCartPosition(position, productsByExternalId))
                .collect(Collectors.toList());
            loadOffers();
            this.lastOrders = orders;
            filteredOrders = new ArrayList<>(orders);
        }
    }

    public boolean hasAllSpecialPrice() {
        return orders.stream()
                .anyMatch(cartRow -> cartRow.getSalesOffer().getSalesOffers().stream().anyMatch(this::hasSpecialPrice));
    }

    public boolean hasSpecialPrice(SalesOffer offer) {
        double personalPrice = offer.getPersonalPrice();
        double marketPrice = offer.getPrice();
        return personalPrice > 0 && personalPrice < marketPrice;
    }

    public String renderSpecialPrice(SalesOffer offer) {
        return " — " + convertCurrency(offer.getPersonalPrice()) + " " + preferredCurrency.getSign();
    }

    public void checkOffersForAllRows() {
        boolean rowWithoutOfferExists = orders.stream()
            .map(CartRow::getSelectedOffer)
            .anyMatch(Objects::isNull);
        if (rowWithoutOfferExists) {
            LocalizedFacesMessage.warn("itc.cart.offer-not-selected");
        }
    }

    public void createOrder() {
        long cartOwnerId = currentCart.getOwnerId();
        ExtendedOrganization contractor = LiferayUtil.findContractorForUser(cartOwnerId);
        if (contractor == null) {
            log.error("User {0,number,integer} has no contractor!", cartOwnerId);
            LocalizedFacesMessage.fatal("itc.cart.user-has-no-contractor");
            return;
        }
        User user;
        try {
            user = UserLocalServiceUtil.getUser(cartOwnerId);
        } catch (PortalException exc) {
            log.error("Can not fetch user {0,number,integer}", cartOwnerId, exc);
            LocalizedFacesMessage.fatal("itc.cart.can-not-fetch-user");
            return;
        }
        List<Requester.PlaceOrderService.SupplyOrder> supplies = order
            .stream()
            .filter(position -> position.getSelectedOffer() != null)
            .filter(position -> position.getSelectedOffer().getOfferId() != null)
            .filter(position -> Objects.equals(position.getSelectedOffer().getCurrencyCode(), preferredCurrency.getId())
                && Objects.equals(position.getSelectedOffer().getWarehouseId(), selectedWarehouse.getCode()))
            .map(position -> new Requester.PlaceOrderService.SupplyOrder(
                position.getSelectedOffer().getOfferId().toString(),
                position.getExternalProductId(), position.getCount())
            )
            .collect(Collectors.toList());
        String backOfficeCode = contractor.getBackOfficeCode();
        String fullName = user.getFullName();
        String cartId = currentCart.getReadableId();
        String comments = orderComment;
        log.debug("Send place order: {0} {1} {2} {3} {4}", backOfficeCode, fullName, cartId, comments);
        log.debug("Order: {0}", supplies);
        Response<List<Requester.PlaceOrderService.PlaceOrderResponse>> response;
        try {
            var call = placeOrderService.placeOrder(new Requester.PlaceOrderService.PlaceOrderRequest(backOfficeCode,
                fullName, cartId, comments, discountComment, encodeFiles(uploadedFiles), supplies));
            response = call.execute();
        } catch (IOException ioe) {
            log.warn("Error in service: {0}", ioe.getMessage());
            LocalizedFacesMessage.error("itc.cart.can-not-place-order");
            return;
        }
        List<Requester.PlaceOrderService.PlaceOrderResponse> createdOrders = response.body();
        if (!response.isSuccessful() || createdOrders == null || createdOrders.isEmpty()) {
            ResponseBody errorBody = response.errorBody();
            String message = null;
            if (errorBody != null) {
                try {
                    message = errorBody.string();
                } catch (IOException ioe) {
                    log.warn(ioe.getMessage());
                }
            }
            log.warn("Error in service. Code {0}, body {1}", response.code(), message);
            LocalizedFacesMessage.error("itc.cart.can-not-place-order");
            return;
        }
        log.info("Order {0} was created!", createdOrders);
        this.createdOrders = createdOrders;
        lockAjaxPolling = true;
        String description = generateDescription(orders);
        this.currentCart.setDescription(description);
        fillCartName(this.currentCart, description);
        this.currentCart = this.cartService.save(this.currentCart);
        this.lastCart = this.currentCart;
        List<CartPosition> positions = cartPositionService.findForCart(this.lastCart);
        double total = CartUtil.computeTotal(positions, preferredCurrency, selectedWarehouse);
        this.currentCart = this.cartService.removeFromCurrent(this.currentCart, Cart.CartState.DELETED, total,
            preferredCurrency, selectedWarehouse);
        actionNeeded = true;
        resetOrder();

    }

    private List<Requester.PlaceOrderService.AttachmentFile> encodeFiles(List<UploadedFile> uploadedFiles) {
        List<Requester.PlaceOrderService.AttachmentFile> resultList = new ArrayList<>();

        for (UploadedFile file : uploadedFiles) {
            String fileBase64 = new String(Base64.getEncoder().encode(file.getContents()));
            String extension;
            if (file.getFileName().contains(".")) {
                String[] split = file.getFileName().split("\\.");
                extension = split[split.length - 1];
            } else {
                extension = file.getContentType();
            }

            resultList.add(new Requester.PlaceOrderService.AttachmentFile(fileBase64, extension));
        }
        return resultList;
    }

    private void loadOffers() {
        List<SalesOffersResponse> salesOffersResponses = OffersUtil.loadOffers(order, salesOffersService,
            preferredCurrency, selectedWarehouse);
        List<SalesOffer> createdOffers = salesOffersResponses.stream()
            .map(SalesOffersResponse::getSalesOffers)
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());
        this.offerConverter = new ByIdConverter<>(createdOffers, SalesOffer::getSalesOfferId);
        Map<String, SalesOffersResponse> offersByProduct = salesOffersResponses.stream()
            .collect(Collectors.toMap(SalesOffersResponse::getId, x -> x, (a, b) -> a));
        for (CartRow cartRow : orders) {
            SalesOffersResponse salesOffer = offersByProduct.get(cartRow.getPcode());
            cartRow.setSalesOffer(salesOffer);
            if (salesOffer != null) {
                ProductOffer selectedProductOffer = cartRow.getOriginal().getSelectedOffer();
                SalesOffer selectedOffer = null;
                if (selectedProductOffer != null && selectedProductOffer.getOfferId() != null) {
                    String selectedOfferId = selectedProductOffer.getOfferId().toString();
                    log.debug("Try to get offer by {0}", selectedOfferId);
                    selectedOffer = offerConverter.get(selectedOfferId);
                }
                log.debug("Selected offer: {0}", selectedOffer);
                if (selectedOffer != null) {
                    cartRow.setSelectedOffer(selectedOffer);
                }

            }
            updateAmount(cartRow);
        }
    }

    private String generateDescription(List<CartRow> orders) {
        return orders.stream()
            .map(CartRow::getPcode)
            .map(productsByExternalId::get)
            .filter(Objects::nonNull)
            .map(phs -> phs.getVendorCode() + " " + phs.getProductGroup().getName())
            .collect(Collectors.joining(", "));
    }

    private void fillCartName(Cart cart, String description) {
        if (cart.getCartName() == null || cart.getCartName().trim().isEmpty()) {
            cart.setCartName(StringUtil.shorten(description, 100));
        }
    }

    public Double getTotalPrice() {
        return orders.stream().mapToDouble(CartRow::getTotalPrice).sum();
    }

    public void removeItem(CartRow order) {
        log.debug("Remove order {0}", order);
        cartPositionService.delete(cartPositionService.attachToManager(order.getOriginal()));
        log.debug("Positions size before removing: {0}", orders.size());
        orders.remove(order);
        filteredOrders.remove(order);
        log.debug("Positions size after removing: {0}", orders.size());
        lastRemovedItem = order;
        LocalizedFacesMessage.info("itc.cart.item.removed");
    }

    public void deleteCart() {
        log.debug("Delete cart {0}", lastCart);
        if (lastCart != null) {
            try {
                String description = generateDescription(lastOrders);
                fillCartName(lastCart, description);
                List<CartPosition> currentOrders = cartPositionService.findForCart(currentCart);
                double total = CartUtil.computeTotal(currentOrders, preferredCurrency, selectedWarehouse);
                this.lastCart = cartService.removeFromCurrent(lastCart, Cart.CartState.DELETED, total,
                    preferredCurrency, selectedWarehouse);
                resetOrder();
                actionNeeded = false;
                LocalizedFacesMessage.info("itc.cart.deleted");
            } catch (Exception e) {
                log.error(e);
                LocalizedFacesMessage.error("itc.cart.deletion-error");
            }

        }
    }

    public void saveCart() {
        log.debug("Save cart {0}", lastCart);
        if (lastCart != null) {
            try {
                String description = generateDescription(lastOrders);
                fillCartName(lastCart, description);
                List<CartPosition> currentOrders = cartPositionService.findForCart(currentCart);
                double total = CartUtil.computeTotal(currentOrders, preferredCurrency, selectedWarehouse);
                this.lastCart = cartService.removeFromCurrent(lastCart, Cart.CartState.ACTIVE, total,
                    preferredCurrency, selectedWarehouse);
                resetOrder();
                savingCompleted = true;
                LocalizedFacesMessage.info("itc.cart.saved");
            } catch (Exception e) {
                log.error(e);
                LocalizedFacesMessage.error("itc.cart.saving-error");
            }

        }
    }

    public void updateSelectedOffer(CartRow cartRow) {
        log.debug("Update offer for product {0} {1} to {2}", cartRow.getPcode(),
            cartRow.getOriginal().getProductDescription(),
            Optional.ofNullable(cartRow.getSelectedOffer()).map(SalesOffer::getSalesOfferId).orElse(null));
        var newPosition = cartPositionService.updateOffer(cartRow.getOriginal(), cartRow.getSelectedOffer(),
            cartRow.getSalesOffer().getCurrencyId(), cartRow.getSalesOffer().getWarehouseId());
        cartRow.setOriginal(newPosition);
        updateAmount(cartRow);
    }


    public Long getMaxCount(CartRow cartRow) {
        return cartRow.getSelectedOffer().getStockAmount() != 0
            ? cartRow.getSelectedOffer().getStockAmount() : Long.MAX_VALUE;
    }

    public Long getMinCount(CartRow cartRow) {
        if (cartRow.getSelectedOffer().getStockAmount() == 0) {
            return Long.valueOf(cartRow.getSelectedOffer().getMinimalOrder());
        }
        return Long.valueOf(cartRow.getSelectedOffer().getMinimalOrder() > cartRow.getSelectedOffer().getStockAmount() ?
            cartRow.getSelectedOffer().getStockAmount() : cartRow.getSelectedOffer().getMinimalOrder());
    }

    public void updateAmount(CartRow cartRow) {
        log.debug("Update amount of product {0}", cartRow);
        if (cartRow.getSelectedOffer() == null) {
            return;
        } else if (cartRow.getAmount() > getMaxCount(cartRow)) {
            cartRow.setAmount(getMaxCount(cartRow));
        } else if (cartRow.getAmount() < getMinCount(cartRow)) {
            cartRow.setAmount(getMinCount(cartRow));
        } else if (cartRow.getAmount() % cartRow.getSelectedOffer().getMultiplicity() != 0) {
            cartRow.setAmount(cartRow.getAmount() - (cartRow.getAmount() % cartRow.getSelectedOffer().getMultiplicity())
                + cartRow.getSelectedOffer().getMultiplicity());
        }
        cartRow.getOriginal().setCount(cartRow.getAmount());
        cartPositionService.save(cartRow.getOriginal());
    }

    private void selectOfferByComparator(Comparator<SalesOffer> comparator) {
        if (this.orders != null) {
            for (CartRow cartRow : this.orders) {
                if (cartRow.getSalesOffer() != null) {
                    List<SalesOffer> offers = cartRow.getSalesOffer().getSalesOffers();
                    if (offers != null) {
                        SalesOffer fastest = offers.stream().min(comparator).orElse(null);
                        cartRow.setSelectedOffer(fastest);
                        updateSelectedOffer(cartRow);
                    }
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

    public Integer getMaxDays() {
        if (this.orders != null) {
            return this.orders.stream()
                .map(CartRow::getSelectedOffer)
                .filter(Objects::nonNull)
                .map(SalesOffer::getDaysDuration)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        }
        return null;
    }

    public void updateName() {
        currentCart = cartService.save(currentCart);
    }

    private void resetOrder() {
        this.productsByExternalId = new HashMap<>();
        this.order = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.lastRemovedItem = null;
        this.currentCart = null;
    }

    private boolean hasViewAccess() {

        //TODO: additional logic (users of same contractor, admins, etc.)
        if (currentCart != null) {
            if (!LiferayPortletHelperUtil.getThemeDisplay().isSignedIn()) {
                return false;
            }
            if (isOwner() || isSalesManager()) {
                return true;
            }
            long ownerId = currentCart.getOwnerId();
            ExtendedOrganization ownerContractor = LiferayUtil.findContractorForUser(ownerId);
            ExtendedOrganization userContractor = LiferayUtil
                .findContractorForUser(LiferayPortletHelperUtil.getUserId());
            if (ownerContractor == null || userContractor == null) {
                return false;
            }
            return ownerContractor.getOrganization().getOrganizationId()
                == userContractor.getOrganization().getOrganizationId();
        }
        return true;
    }

    public boolean isContractorEmployee() {
        return RoleChecker.hasAnyGroupRole(user.getUserId(),
            RoleConstants.CONTRACTOR_EMPLOYEE, RoleConstants.CONTRACTOR_ADMIN);
    }

    public boolean isSalesManager() {
        return RoleChecker.hasAnyGlobalRole(LiferayPortletHelperUtil.getUserId(),
            RoleConstants.SALES_MANAGER, RoleConstants.SALES_DEPARTMENT_HEAD);
    }

    public boolean isEditable() {
        return currentCart != null && currentCart.isCurrent()
            && (currentCart.getOwnerId() == user.getUserId() || isSalesManager());
    }

    public String convertCurrency(double value) {
        return String.format("%,.2f", value);
    }

    public boolean isOwner() {
        return currentCart != null && currentCart.getOwnerId() == user.getUserId();
    }

    public String convertStocksAmount(SalesOffer offer) {
        if (offer.getStockAmount() > 0) {
            return offer.getStockAmount() + "";
        }
        return "много";
    }

    public String getCatalogueLink() throws PortalException {
        LiferayPortletURL catalogueUrl = LiferayUtil.getPortletUrl(PortletIdConstants.CATALOGUE);
        return catalogueUrl.toString();
    }

    public String getProfileLink() throws PortalException {
        LiferayPortletURL profileUrl = LiferayUtil.getPortletUrl(PortletIdConstants.PROFILE);
        return profileUrl.toString();
    }

    public String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy").format(date);
    }

}
