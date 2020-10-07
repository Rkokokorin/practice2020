package com.tuneit.itc.contacts;

import com.liferay.faces.portal.el.internal.Liferay;
import com.tuneit.itc.commons.model.OfficeContactInfo;
import com.tuneit.itc.commons.service.OfficeContactInfoService;
import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.persistence.Column;
import javax.validation.constraints.Pattern;

@Data
@ViewScoped
@ManagedBean
public class ContactsBean {
    private static final String DEFAULT_CITY = "Санкт-Петербург";
    private String shownCity;

    private OfficeContactInfo contactInfo;
    private OfficeContactInfo newContactInfo;

    @ManagedProperty("#{officeContactInfoService}")
    private OfficeContactInfoService officeContactInfoService;
    @ManagedProperty("#{liferay}")
    private Liferay liferay;

    private Mode mode;
    private String text;

    @PostConstruct
    public void init() {
        updateShownCity(DEFAULT_CITY);
    }

    public void updateShownCity(String city) {
        shownCity = city;
        contactInfo = officeContactInfoService.findByCity(shownCity);
        //update view too
        setModeView();
    }

    public void createCity() {
        shownCity = newContactInfo.getCity();
        contactInfo = newContactInfo;
        setModeView();
        //TODO reassign newContactInfo to new()
    }

    public boolean isAdmin() {
        return liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
    }

    public boolean isModeCreate() {
        return mode == Mode.CREATE;
    }

    public boolean isModeView() {
        return mode == Mode.VIEW;
    }

    public void setModeCreate() {
        mode = Mode.CREATE;
    }

    public void setModeView() {
        mode = Mode.VIEW;
    }

    private enum Mode {
        VIEW,
        UPDATE,
        CREATE
    }
}
