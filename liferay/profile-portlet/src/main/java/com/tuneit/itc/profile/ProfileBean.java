package com.tuneit.itc.profile;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.tuneit.itc.commons.model.ExtendedOrganization;
import com.tuneit.itc.commons.model.rest.OrderHistoryItem;
import com.tuneit.itc.commons.service.rest.Requester;

@Data
@ViewScoped
@ManagedBean
public class ProfileBean {

    Logger log = LoggerFactory.getLogger(ProfileBean.class);
    private User user;
    private String name;
    private String email;
    private String organizationName;
    private String contractorTin;
    private String contractorMsrn;
    private boolean hasContractor;
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

    @PostConstruct
    public void init() {
        user = liferay.getThemeDisplay().getUser();
        loadUserOrganization();
        loadUserData();
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

    private void loadUserData() {
        name = user.getFullName();
        email = user.getEmailAddress();
        if (organization == null) {
            organizationName = "ITC"; // TODO
            hasContractor = false;
        } else {
            hasContractor = true;
            organizationName = organization.getName();
        }
    }

    private void loadContractorData() {
        if (organization != null) {
            extendedOrg = new ExtendedOrganization(organization);
            if (!extendedOrg.isJuridicalPerson()) {
                hasContractor = false;
                return;
            }
            hasContractor = true;
            contractorTin = extendedOrg.getTin();
            contractorMsrn = extendedOrg.getMsrn();
        }
    }

    public String formatDate(OrderHistoryItem order) {
        if (order == null || order.getDate() == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy.MM.dd").format(order.getDate());
    }
}
