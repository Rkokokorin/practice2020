package com.tuneit.itc.zuulproxy.metrics;

import javax.management.MBeanAttributeInfo;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.tuneit.itc.zuulproxy.metrics.beans.TimeMetrics;

public class Utils {
    public static final String AVG_PREFIX = "Average ";
    public static final String COUNT_PREFIX = "Count ";

    public static MBeanAttributeInfo avgAttributeFromUrl(String url) {
        return new MBeanAttributeInfo(AVG_PREFIX + url, long.class.getSimpleName(), "Average time for URI " + url, true, false, false);
    }

    public static MBeanAttributeInfo countAttributeFromUrl(String url) {
        return new MBeanAttributeInfo(COUNT_PREFIX + url, int.class.getSimpleName(), "Total count of requests for URI " + url, true, false, false);
    }

    public static List<MBeanAttributeInfo> attributesFromUrls(Collection<String> urls) {
        List<MBeanAttributeInfo> infos = urls
                .stream()
                .map(Utils::avgAttributeFromUrl)
                .collect(Collectors.toList());
        urls.stream()
                .map(Utils::countAttributeFromUrl)
                .forEach(infos::add);
        return infos;
    }

    public static Object getAttributeValue(TimeMetrics metrics, String key) {
        if (key.startsWith(AVG_PREFIX)) {
            String url = key.replaceFirst(AVG_PREFIX, "");
            return metrics.getAverageMsByUrl(url);
        } else if (key.startsWith(COUNT_PREFIX)) {
            String url = key.replaceFirst(COUNT_PREFIX, "");
            return metrics.getCountByUrl(url);
        }
        return null;
    }

    private Utils() {
        throw new IllegalStateException("This is an utility class!");
    }
}
