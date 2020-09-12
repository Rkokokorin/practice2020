package profile;

import lombok.Data;
import retrofit2.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;

import com.tuneit.itc.commons.jsf.ParamNameConstants;
import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.rest.OrderHistoryItem;
import com.tuneit.itc.commons.service.rest.Requester;

/**
 * Single order view.
 *
 * @author Alexander Pashnin
 */
@Data
@ViewScoped
@ManagedBean
public class OrdersBean {

    Logger log = LoggerFactory.getLogger(OrdersBean.class);
    private User user;
    private Organization organization;
    private ExtendedOrganization extendedOrg;
    private List<OrderHistoryItem> orders = new ArrayList<>();
    private List<OrderHistoryItem> filteredOrders = new ArrayList<>();
    private Date dateFrom;
    private Date dateTo;
    @ManagedProperty("#{liferay}")
    private Liferay liferay;
    @ManagedProperty("#{requester.orderHistoryService}")
    private Requester.OrderHistoryService orderHistoryService;
    @ManagedProperty("#{requester.orderStatusService}")
    private Requester.OrderStatusService orderStatusService;
    private String contractorId;

    @PostConstruct
    public void init() {
        user = liferay.getThemeDisplay().getUser();
        loadUserOrganization();
        if (extendedOrg != null) {
            contractorId = extendedOrg.getBackOfficeCode();
            loadOrders();
        }
        loadContractorData();
    }


    private void loadUserOrganization() {
        List<Organization> userOrganizations = OrganizationLocalServiceUtil
            .getUserOrganizations(user.getUserId());
        if (null == userOrganizations || userOrganizations.isEmpty()) {
            organization = null;
            extendedOrg = null;
        } else {
            organization = userOrganizations.get(0);
            extendedOrg = new ExtendedOrganization(organization);
        }
    }


    private void loadContractorData() {
        if (organization != null) {
            extendedOrg = new ExtendedOrganization(organization);
            if (!extendedOrg.isJuridicalPerson()) {
                return;
            }
        }
    }

    private void loadOrders() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -1);
            Response<List<OrderHistoryItem>> execute = orderHistoryService
                .getByIdInInterval(contractorId,
                    new OrderHistoryItem.DateWrapper(calendar.getTime()))
                .execute();
            if (!execute.isSuccessful() || execute.body() == null) {
                log.error("Can't fetch orders history for contractor with id: " + contractorId
                    + ". Response is " + execute);
                orders = new ArrayList<>();
            } else {
                orders = execute.body()
                    .stream()
                    .sorted(
                        Comparator.comparing(
                            (OrderHistoryItem item) -> item.getDate().toInstant().truncatedTo(ChronoUnit.DAYS)
                        )
                        .reversed()
                        .thenComparing(OrderHistoryItem::getNumber)
                    )
                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            orders = new ArrayList<>();
            log.error("Can't fetch orders history for contractor with id: " + contractorId);
            log.error(e);
        }
        filteredOrders = new ArrayList<>(orders);
    }

    public String openOrder(OrderHistoryItem order) {
        return String.format("/WEB-INF/views/order-view.xhtml?faces-redirect=true&%s=%s",
            ParamNameConstants.ORDER, order.getId()
        );
    }

    public String formatDate(OrderHistoryItem order) {
        if (order == null || order.getDate() == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy.MM.dd").format(order.getDate());
    }
}
