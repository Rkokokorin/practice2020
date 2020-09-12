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

import com.tuneit.itc.zuulproxy.metrics.Utils;

@Component
public class RoutedTimeMetricsStatistics implements DynamicMBean {

    @Autowired
    private TimeMetrics metrics;

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
        List<String> urls = metrics.getRoutedStatisticsUrls();
        List<MBeanAttributeInfo> infos = Utils.attributesFromUrls(urls);
        return new MBeanInfo(RoutedTimeMetricsStatistics.class.getName(), null, infos.toArray(MBeanAttributeInfo[]::new), null, null, new MBeanNotificationInfo[0]);
    }

}
