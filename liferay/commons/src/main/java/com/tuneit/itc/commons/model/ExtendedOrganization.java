package com.tuneit.itc.commons.model;

import lombok.Data;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Organization;

@Data
public class ExtendedOrganization {

    public static final String CODE_ALPHABET = "авекмнорстух";
    public static final int MIN_CODE_LENGTH = 4;
    private final Organization organization;
    private final Log log = LogFactoryUtil.getLog(ExtendedOrganization.class);

    public String getTin() {
        return OrganizationCustomFields.TIN.getValue(organization);
    }

    public void setTin(String value) {
        OrganizationCustomFields.TIN.setValue(organization, value);
    }

    public String getMsrn() {
        return OrganizationCustomFields.MSRN.getValue(organization);
    }

    public void setMsrn(String value) {
        OrganizationCustomFields.MSRN.setValue(organization, value);
    }

    public boolean isJuridicalPerson() {
        return OrganizationCustomFields.IS_JURIDICAL_PERSON.getValue(organization);
    }

    public void setJuridicalPerson(boolean value) {
        OrganizationCustomFields.IS_JURIDICAL_PERSON.setValue(organization, value);
    }

    public String getShortName() {
        return OrganizationCustomFields.COMPANY_SHORT_NAME.getValue(organization);
    }

    public void setShortName(String shortName) {
        OrganizationCustomFields.COMPANY_SHORT_NAME.setValue(organization, shortName);
    }

    public String getBackOfficeCode() {
        return OrganizationCustomFields.COMPANY_BACK_OFFICE_CODE.getValue(organization);
    }

    public void setBackOfficeCode(String code) {
        OrganizationCustomFields.COMPANY_BACK_OFFICE_CODE.setValue(organization, code);
    }

    public boolean isProvider() {
        return OrganizationCustomFields.COMPANY_IS_PROVIDER.getValue(organization);
    }

    public void setProvider(boolean value) {
        OrganizationCustomFields.COMPANY_IS_PROVIDER.setValue(organization, value);
    }

    public boolean isBuyer() {
        return OrganizationCustomFields.COMPANY_IS_BUYER.getValue(organization);
    }

    public void setBuyer(boolean value) {
        OrganizationCustomFields.COMPANY_IS_BUYER.setValue(organization, value);
    }

    public Long getSyntheticId() {
        return OrganizationCustomFields.SYNTHETIC_ID.getValue(organization);
    }

    public void setSyntheticId(Long syntheticId) {
        OrganizationCustomFields.SYNTHETIC_ID.setValue(organization, syntheticId);
    }

    public String getSyntheticCode() {
        Long syntheticId = getSyntheticId();
        if (syntheticId == null) {
            syntheticId = 0L;
        }
        StringBuilder code = new StringBuilder();
        while (syntheticId > 0L) {
            int charIdx = (int) (syntheticId % CODE_ALPHABET.length());
            char nextChar = CODE_ALPHABET.charAt(charIdx);
            code.append(nextChar);
            syntheticId /= CODE_ALPHABET.length();
        }
        while (code.length() < MIN_CODE_LENGTH) {
            code.append(CODE_ALPHABET.charAt(0));
        }
        code.reverse();
        return code.toString();
    }

    public String getPhone() {
        return OrganizationCustomFields.PHONE.getValue(organization);
    }

    public void setPhone(String phone) {
        OrganizationCustomFields.PHONE.setValue(organization, phone);
    }

}
