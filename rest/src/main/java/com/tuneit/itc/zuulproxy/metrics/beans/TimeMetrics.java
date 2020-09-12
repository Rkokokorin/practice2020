package com.tuneit.itc.zuulproxy.metrics.beans;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TimeMetrics implements TimeMetricsMBean {

    private AtomicBoolean metricsEnables = new AtomicBoolean(false);

    private Map<String, List<Duration>> timeMetrics = new ConcurrentHashMap<>();
    private Map<String, List<Duration>> routedTimeMetrics = new ConcurrentHashMap<>();

    public void clear() {
        routedTimeMetrics.clear();
        timeMetrics.clear();
    }

    private void add(String url, Duration metric, Map<String, List<Duration>> target) {
        if (this.metricsEnables.get()) {
            target
                    .computeIfAbsent(url, key -> Collections.synchronizedList(new ArrayList<>()))
                    .add(metric);
        }
    }


    public void add(String url, String redirectUrl, Instant start, Instant end) {
        Duration duration = Duration.between(start, end);
        this.add(url, duration, timeMetrics);
        this.add(redirectUrl, duration, routedTimeMetrics);
    }


    @Override
    public boolean isMetricsEnabled() {
        return metricsEnables.get();
    }

    @Override
    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnables.set(metricsEnabled);
    }

    public long getAverageMsByUrl(String url) {
        if (timeMetrics.containsKey(url)) {
            return getAverage(url, timeMetrics);
        }
        return getAverage(url, routedTimeMetrics);
    }

    public int getCountByUrl(String url) {
        if (timeMetrics.containsKey(url)) {
            return getCount(url, timeMetrics);
        }
        return getCount(url, routedTimeMetrics);
    }

    public List<String> getRoutedStatisticsUrls() {
        return new ArrayList<>(routedTimeMetrics.keySet());
    }

    private long getAverage(String url, Map<String, List<Duration>> target) {
        return (long) target.getOrDefault(url, Collections.emptyList())
                .stream()
                .mapToLong(Duration::toMillis)
                .average()
                .orElse(0.0);
    }

    private int getCount(String url, Map<String, List<Duration>> target) {
        return target.getOrDefault(url, Collections.emptyList()).size();

    }
}
