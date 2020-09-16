package com.tuneit.itc.contacts.contractors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import com.tuneit.itc.commons.model.ExtendedOrganization;

@ManagedBean
@ViewScoped
@Data
@EqualsAndHashCode(exclude = {"users"})
@ToString(exclude = {"users"})
public class ContractorViewBean implements Serializable {

    private ExtendedOrganization selectedContractor = null;
    private boolean editMode = false;
    private List<User> users;
    private Log log = LogFactoryUtil.getLog(this.getClass());

    @ManagedProperty("#{contractorRegistrationBean}")
    private ContractorRegistrationBean contractorRegistrationBean;

    public User getAdmin() {
        return users == null || users.isEmpty() ? null : users.get(0);
    }

    @PostConstruct
    public void init() {
    }

    public boolean isViewMode() {
        return selectedContractor != null && !editMode;
    }

    public boolean isEditMode() {
        return selectedContractor != null && editMode;
    }

    public void selectContractor(Organization contractor) {
        selectedContractor = new ExtendedOrganization(contractor);
        users = UserLocalServiceUtil.getOrganizationUsers(contractor.getOrganizationId());
    }

    public void goBack() {
        resetSelectedContractor();
    }

    public void updateUsers() {
        users = UserLocalServiceUtil.getOrganizationUsers(selectedContractor.getOrganization().getOrganizationId());
    }

    public void resetSelectedContractor() {
        selectedContractor = null;
        editMode = false;
    }

    public void updateSelectedContractor() {
        log.info(contractorRegistrationBean);
        if (!contractorRegistrationBean.validateFields()) {
            return;
        }
        if (selectedContractor.isJuridicalPerson()) {
            selectedContractor.setMsrn(contractorRegistrationBean.getMsrn());
            selectedContractor.setTin(contractorRegistrationBean.getTin());
            selectedContractor.setShortName(contractorRegistrationBean.getShortName());
            selectedContractor.getOrganization().setName(contractorRegistrationBean.getName());
        }
        selectedContractor.setBackOfficeCode(contractorRegistrationBean.getBackOfficeCode());
        selectedContractor.setPhone(contractorRegistrationBean.getPhone());
        OrganizationLocalServiceUtil.updateOrganization(selectedContractor.getOrganization());
        selectedContractor = null;
        editMode = false;
        contractorRegistrationBean.init();
    }

    public void setEditMode(Boolean editMode) {
        if (null == selectedContractor) {
            throw new IllegalStateException("Attempt to edit contractor without a selected one");
        }
        this.editMode = editMode;
        if (editMode) {
            contractorRegistrationBean.setLegalEntity(
                    selectedContractor.isJuridicalPerson());
            if (selectedContractor.isJuridicalPerson()) {
                contractorRegistrationBean.setMsrn(selectedContractor.getMsrn());
                contractorRegistrationBean.setTin(selectedContractor.getTin());
                contractorRegistrationBean.setShortName(selectedContractor.getShortName());
                contractorRegistrationBean.setName(selectedContractor.getOrganization().getName());
            }
            contractorRegistrationBean.setPhone(selectedContractor.getPhone());
            contractorRegistrationBean.setBackOfficeCode(selectedContractor.getBackOfficeCode());
        }
    }
}
