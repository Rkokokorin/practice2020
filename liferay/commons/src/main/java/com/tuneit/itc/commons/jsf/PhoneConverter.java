package com.tuneit.itc.commons.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("phoneConverter")
public class PhoneConverter implements Converter {
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object modelValue) {
        String phoneNumber = (String) modelValue;
        StringBuilder formattedPhoneNumber = new StringBuilder();

        formattedPhoneNumber.append(phoneNumber.substring(0, 2)).append(" ").append(phoneNumber.substring(2, 5))
                .append(" ").append(phoneNumber.substring(5, 8)).append(" ").append(phoneNumber.charAt(8))
                .append(" ").append(phoneNumber.substring(9));

        return formattedPhoneNumber.toString();
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String submittedValue) {
        // Conversion is not necessary for now. However, if you ever intend to use
        // it on input components, you probably want to implement it here.
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
