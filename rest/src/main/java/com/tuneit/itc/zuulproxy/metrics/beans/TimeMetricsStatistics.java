package com.tuneit.itc.zuulproxy.metrics.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ReflectionException;
import java.util.List;
import java.util.stream.Collectors;

import com.tuneit.itc.zuulproxy.metrics.Utils;
import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import com.tuneit.itc.zuulproxy.repositories.CustomZuulRouteRepository;

@Component
public class TimeMetricsStatistics implements DynamicMBean {
    public static final String AVG_PREFIX = "Average ";
    public static final String COUNT_PREFIX = "Count ";

    @Autowired
    private TimeMetrics metrics;
    @Autowired
    private CustomZuulRouteRepository zuulRouteRepository;

    @Override
    public Object getAttribute(String s) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return getDynamicAttribute(s);
    }

    private Object getDynamicAttribute(String key) {
        return Utils.getAttributeValue(metrics, key);
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    }

    @Override
    public AttributeList getAttributes(String[] strings) {
        AttributeList result = new AttributeList();
        for (String key : strings) {
            result.add(new Attribute(key, getDynamicAttribute(key)));
        }
        return result;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributeList) {
        return null;
    }

    @Override
    public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        List<CustomZuulRoute> allRoutes = zuulRouteRepository.findAll();
        List<String> urls = allRoutes.stream().map(CustomZuulRoute::getPath).collect(Collectors.toList());
        List<MBeanAttributeInfo> infos = Utils.attributesFromUrls(urls);
        return new MBeanInfo(TimeMetricsStatistics.class.getName(), null, infos.toArray(MBeanAttributeInfo[]::new), null, null, new MBeanNotificationInfo[0]);
    }

}
