package com.tuneit.itc.commons.util;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.liferay.portal.kernel.model.Organization;

import com.tuneit.itc.commons.model.OrganizationCustomFields;

@ManagedBean(eager = true)
@ApplicationScoped
public class OrganizationColumnsInitializer {
    @PostConstruct
    public void init() {
        ExpandoColumnInitializer.initializeExpandoTable(Organization.class,
            Arrays.asList(OrganizationCustomFields.TIN, OrganizationCustomFields.MSRN,
                OrganizationCustomFields.IS_JURIDICAL_PERSON, OrganizationCustomFields.COMPANY_SHORT_NAME,
                OrganizationCustomFields.COMPANY_BACK_OFFICE_CODE, OrganizationCustomFields.COMPANY_IS_BUYER,
                OrganizationCustomFields.COMPANY_IS_PROVIDER, OrganizationCustomFields.SYNTHETIC_ID,
                OrganizationCustomFields.PHONE)
        );
    }
}
