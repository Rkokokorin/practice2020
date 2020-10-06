package com.tuneit.itc.contacts;

import com.liferay.faces.portal.el.internal.Liferay;
import com.tuneit.itc.commons.model.OfficeContactInfo;
import com.tuneit.itc.commons.service.OfficeContactInfoService;
import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@Data
@ViewScoped
@ManagedBean
public class ContactsViewBean {
    private static final String DEFAULT_CITY = "Санкт-Петербург";
    private String shownCity;
    private OfficeContactInfo contactInfo;

    @ManagedProperty("#{officeContactInfoService}")
    private OfficeContactInfoService officeContactInfoService;
    @ManagedProperty("#{liferay}")
    private Liferay liferay;

    private Mode mode;

    @PostConstruct
    public void init() {
        updateShownCity(DEFAULT_CITY);
    }

    public void updateShownCity(String city) {
        shownCity = city;
        contactInfo = officeContactInfoService.findByCity(shownCity);
        //update view too
        mode = Mode.VIEW;
    }

    public boolean isAdmin() {
        return liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
    }

    public boolean isModeEdit() {
        return mode == Mode.EDIT;
    }

    public boolean isModeView() {
        return mode == Mode.VIEW;
    }

    public void setModeEdit() {
        mode = Mode.EDIT;
    }

    public void setModeView() {
        mode = Mode.VIEW;
    }

    private enum Mode {
        VIEW,
        EDIT
    }
}
