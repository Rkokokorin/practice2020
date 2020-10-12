package com.tuneit.itc.contacts;

import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.tuneit.itc.commons.model.OfficeContactInfo;
import com.tuneit.itc.commons.model.StafferContactInfo;
import com.tuneit.itc.commons.service.StafferContactInfoService;
import lombok.Data;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.*;

@Data
@ViewScoped
@ManagedBean
public class StaffersContactInfoBean {
    Logger log = LoggerFactory.getLogger(OfficeContactInfoBean.class);

    @ManagedProperty("#{officeContactInfoBean.contactInfo}")
    private OfficeContactInfo currentOffice;

    private List<StafferContactInfo> staffers;
    private List<String> departments;
    private StafferContactInfo newContactInfo;

    @ManagedProperty("#{stafferContactInfoService}")
    private StafferContactInfoService stafferContactInfoService;
    @ManagedProperty("#{liferay}")
    private Liferay liferay;

    private Mode mode;

    @PostConstruct
    public void init() {
        staffers = stafferContactInfoService.getAllOfficeStaffers(currentOffice.getId());
        departments = new ArrayList<>();
        for (StafferContactInfo staffer : staffers) {
            if (!departments.contains(staffer.getDepartment()))
                departments.add(staffer.getDepartment());
        }
        setModeView();
    }

    public void addStaffer() {
        newContactInfo.setOfficeContactInfo(currentOffice);
        stafferContactInfoService.save(newContactInfo);
        staffers.add(newContactInfo);
        departments.add(newContactInfo.getDepartment());
        setModeView();
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
        newContactInfo = new StafferContactInfo();
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
