package com.tuneit.itc.contacts;

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
    private OfficeContactInfo contactInfo;

    @ManagedProperty("#{officeContactInfoService}")
    private OfficeContactInfoService officeContactInfoService;

    @PostConstruct
    public void init() {
        contactInfo = officeContactInfoService.findByCity("Санкт-Петербург");
    }
}
