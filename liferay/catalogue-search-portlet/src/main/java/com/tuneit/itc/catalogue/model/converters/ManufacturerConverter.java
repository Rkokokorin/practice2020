package com.tuneit.itc.catalogue.model.converters;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import com.tuneit.itc.catalogue.model.Manufacturer;

public class ManufacturerConverter implements Converter {
    private final Log log = LogFactoryUtil.getLog(this.getClass());
    private Map<String, Manufacturer> manufacturers;

    public ManufacturerConverter(Map<String, Manufacturer> manufacturers) {
        this.manufacturers = manufacturers;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return manufacturers.get(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }
        return ((Manufacturer) value).getId();
    }
}
