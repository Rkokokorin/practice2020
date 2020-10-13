package com.tuneit.itc.contacts;

import com.google.gson.Gson;
import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.tuneit.itc.commons.model.OfficeContactInfo;
import com.tuneit.itc.commons.service.OfficeContactInfoService;
import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.List;

@Data
@ViewScoped
@ManagedBean
public class OfficeContactInfoBean {
    Logger log = LoggerFactory.getLogger(OfficeContactInfoBean.class);

    private static final String DEFAULT_CITY = "Санкт-Петербург";

    private List<String> existingCities;
    @ManagedProperty("#{cities['names']}")
    private String possibleCities;

    private UIComponent errorMessagesCreateMode;
    private UIComponent errorMessagesUpdateMode;
    @ManagedProperty("#{i18n['itc.admin.contacts.validator-message.city-exists']}")
    private String cityAlreadyExistsMessage;
    @ManagedProperty("#{i18n['itc.admin.contacts.validator-message.city-invalid']}")
    private String notValidCityMessage;

    private OfficeContactInfo contactInfo;
    private OfficeContactInfo newContactInfo;

    @ManagedProperty("#{officeContactInfoService}")
    private OfficeContactInfoService officeContactInfoService;
    @ManagedProperty("#{liferay}")
    private Liferay liferay;

    private Mode mode;

    @PostConstruct
    public void init() {
        log.info("OFFICE INIT");
        existingCities = officeContactInfoService.getNamesOfAllCities();
        contactInfo = officeContactInfoService.findByCity(DEFAULT_CITY);
        setModeView();
    }

    public OfficeContactInfo updateShownCity(String city) {
        contactInfo = officeContactInfoService.findByCity(city);
        return contactInfo;
    }

    public void createCity() {
        if (!existingCities.contains(newContactInfo.getCity()) && possibleCities.contains("%"+newContactInfo.getCity()+"%")) {
            officeContactInfoService.save(newContactInfo);
            contactInfo = newContactInfo;
            existingCities.add(contactInfo.getCity());
            setModeView();
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            if (existingCities.contains(newContactInfo.getCity()))
                context.addMessage(errorMessagesCreateMode.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, cityAlreadyExistsMessage, ""));
            if (!possibleCities.contains("\n"+newContactInfo.getCity()+"\n"))
                context.addMessage(errorMessagesCreateMode.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, notValidCityMessage, ""));
        }
    }

    public void updateCity() {
        if (possibleCities.contains("%"+contactInfo.getCity()+"%")) {
            if (!existingCities.contains(contactInfo.getCity()) ||
                    (contactInfo.getCity().equals(officeContactInfoService.find(contactInfo.getId()).orElse(null).getCity()))) {
                officeContactInfoService.save(contactInfo);
                setModeView();
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(errorMessagesCreateMode.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, cityAlreadyExistsMessage, ""));
                contactInfo.setCity(officeContactInfoService.find(contactInfo.getId()).orElse(null).getCity());
            }
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(errorMessagesUpdateMode.getClientId(context), new FacesMessage(FacesMessage.SEVERITY_ERROR, notValidCityMessage, ""));
            contactInfo.setCity(officeContactInfoService.find(contactInfo.getId()).orElse(null).getCity());
        }
    }

    public String getExistingCitiesAsJson() {
        return new Gson().toJson(existingCities);
    }

    public boolean isAdmin() {
        return liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
    }

    public boolean isModeCreate() {
        return mode == Mode.CREATE;
    }

    public boolean isModeUpdate() {
        return mode == Mode.UPDATE;
    }

    public boolean isModeView() {
        return mode == Mode.VIEW;
    }

    public void setModeCreate() {
        newContactInfo = new OfficeContactInfo();
        mode = Mode.CREATE;
    }

    public void setModeUpdate() {
        mode = Mode.UPDATE;
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
