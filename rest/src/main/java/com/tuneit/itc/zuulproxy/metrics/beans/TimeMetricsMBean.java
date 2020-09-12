package com.tuneit.itc.zuulproxy.metrics.beans;

public interface TimeMetricsMBean {
    boolean isMetricsEnabled();

    void setMetricsEnabled(boolean metricsEnabled);

    void clear();
}
