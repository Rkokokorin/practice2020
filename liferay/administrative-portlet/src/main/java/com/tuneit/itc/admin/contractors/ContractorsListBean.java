package com.tuneit.itc.admin.contractors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import com.tuneit.itc.admin.AdministrativeBean;
import com.tuneit.itc.commons.model.ExtendedOrganization;

@ManagedBean
@ViewScoped
@Data
@EqualsAndHashCode(exclude = {"organizations"})
@ToString(exclude = {"organizations"})
public class ContractorsListBean implements Serializable {

    private List<OrganizationView> organizations;
    private Log log = LogFactoryUtil.getLog(this.getClass());
    @ManagedProperty("#{administrativeBean}")
    private AdministrativeBean administrativeBean;
    @ManagedProperty("#{contractorViewBean}")
    private ContractorViewBean contractorViewBean;

    @PostConstruct
    public void init() {
        updateOrganizations();

        if (!canViewList()) {
            for (OrganizationView organization : organizations) {
                long groupId = organization.getOrganization().getOrganization().getGroupId();
                if (administrativeBean.canViewGivenContractorPage(groupId)) {
                    contractorViewBean.selectContractor(organization.getOrganization().getOrganization());
                    return;
                }
            }
        }
    }

    private void updateOrganizations() {
        this.organizations = OrganizationLocalServiceUtil.getOrganizations(QueryUtil.ALL_POS, QueryUtil.ALL_POS)
                .stream()
                .filter(org -> org.getParentOrganizationId() == 0)
                .sorted(Comparator.comparing(Organization::getName))
                .map(org -> new OrganizationView(new ExtendedOrganization(org),
                        UserLocalServiceUtil.getOrganizationUsersCount(org.getOrganizationId())))
                .collect(Collectors.toList());
    }

    public boolean canViewList() {
        return administrativeBean.canViewContractorList();
    }

    public void returnToList() {
        updateOrganizations();
        contractorViewBean.resetSelectedContractor();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationView {
        private ExtendedOrganization organization;
        private long userCount;
    }
}
