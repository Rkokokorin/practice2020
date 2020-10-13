package com.tuneit.itc.contacts;

import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.tuneit.itc.commons.model.OfficeContactInfo;
import com.tuneit.itc.commons.model.StafferContactInfo;
import com.tuneit.itc.commons.service.StafferContactInfoService;
import lombok.Data;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.shaded.commons.io.IOUtils;
import org.primefaces.util.FileUploadUtils;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.*;

@Data
@ViewScoped
@ManagedBean
public class StaffersContactInfoBean {
    Logger log = LoggerFactory.getLogger(StaffersContactInfoBean.class);

    @ManagedProperty("#{officeContactInfoBean}")
    private OfficeContactInfoBean officeContactInfoBean;
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
        log.info("STAFFERS INIT");
        currentOffice = officeContactInfoBean.getContactInfo();
        staffers = stafferContactInfoService.getAllOfficeStaffers(currentOffice.getId());
        departments = new ArrayList<>();
        for (StafferContactInfo staffer : staffers) {
            if (!departments.contains(staffer.getDepartment()))
                departments.add(staffer.getDepartment());
        }
        setModeView();
    }

    public void updateShownStaffers() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String city = externalContext.getRequestParameterMap().get("city");
        currentOffice = officeContactInfoBean.updateShownCity(city);
        staffers = stafferContactInfoService.getAllOfficeStaffers(currentOffice.getId());
        departments = new ArrayList<>();
        for (StafferContactInfo staffer : staffers) {
            if (!departments.contains(staffer.getDepartment()))
                departments.add(staffer.getDepartment());
        }
    }

    public void addStaffer() {
        newContactInfo.setOfficeContactInfo(currentOffice);
        stafferContactInfoService.save(newContactInfo);
        staffers.add(newContactInfo);
        if (!departments.contains(newContactInfo.getDepartment()))
            departments.add(newContactInfo.getDepartment());
        setModeView();
    }

    public void removeStaffer(StafferContactInfo staffer) {
        stafferContactInfoService.delete(staffer);
        staffers.remove(staffer);
    }

    public void handlePictureUpload(FileUploadEvent event) throws IOException {
        newContactInfo.setPhoto(Base64.getEncoder().encodeToString(event.getFile().getContents()));
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
