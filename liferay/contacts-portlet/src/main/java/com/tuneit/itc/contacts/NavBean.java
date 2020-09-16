package com.tuneit.itc.contacts;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.contacts.util.RequestParam;

@ManagedBean
@RequestScoped
@Data
public class NavBean implements Serializable {

    private final Log log = LogFactoryUtil.getLog(this.getClass());
    private static final RequestParam FACES_REDIRECT_TRUE = new RequestParam("faces-redirect", "true");

    @ManagedProperty("#{administrativeBean}")
    private AdministrativeBean administrativeBean;

    @RequiredArgsConstructor
    public enum Page {
        CONTACTS("/WEB-INF/views/contacts/view.xhtml", "itc.contacts"),
        CLAIMS("/WEB-INF/views/claims/list.xhtml", "itc.admin.navigation.claims"),
        CONTRACTORS("/WEB-INF/views/contractors/list.xhtml", "itc.admin.navigation.contractors"),
        CREATE_CONTRACTOR("/WEB-INF/views/contractors/create-contractor.xhtml",
                "itc.admin.navigation.contractor-creation"),
        CREATE_CLAIM("/WEB-INF/views/claims/create.xhtml", "itc.admin.navigation.claim-creation"),
        PARTNER_CREATION("/WEB-INF/views/contractors/create-contractor.xhtml", "itc.admin.navigation.partner-creation");
        @Getter
        private final String viewName;
        @Getter
        private final String localizationKey;

    }

    public Page getCurrentPage() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return Arrays.stream(Page.values())
                .filter(page -> page.getViewName().equals(viewId))
                .findAny()
                .orElse(null);
    }

    public String goToPage(Page page) {
        return page.viewName;
    }

    public String goToPage(Page page, Collection<RequestParam> params) {
        StringBuilder url = new StringBuilder(page.viewName);
        String concat = "?";
        for (RequestParam param : params) {
            url.append(concat).append(param.toUrlParam());
            concat = "&";
        }
        return url.toString();
    }

    public String goToPage(Page page, RequestParam... params) {
        return goToPage(page, Arrays.asList(params));
    }

    public String redirectToPage(Page page) {
        return goToPage(page, FACES_REDIRECT_TRUE);
    }

    public List<Page> getPages() {
        List<Page> pages = new ArrayList<>();
        if (administrativeBean.canViewContractorList() || administrativeBean.canViewContractorPage()) {
            //TODO split onto view and list pages
            pages.add(Page.CONTRACTORS);
        }
        if (administrativeBean.canViewCreateContractorPage()) {
            pages.add(Page.CREATE_CONTRACTOR);
        }
        if (administrativeBean.canViewClaimsListPage()) {
            pages.add(Page.CLAIMS);
        }
        return pages;
    }

    public String redirectToDefaultPage() {
        return redirectToPage(Page.CONTACTS);
        /*if (administrativeBean.canViewContractorList()) {
            return redirectToPage(Page.CONTRACTORS);
        }
        if (administrativeBean.canViewContractorPage()) {
            return redirectToPage(Page.CONTRACTORS);
        }
        return redirectToPage(Page.CREATE_CLAIM);*/
    }

}
