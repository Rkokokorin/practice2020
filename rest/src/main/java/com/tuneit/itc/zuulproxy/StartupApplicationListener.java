package com.tuneit.itc.zuulproxy;

import com.tuneit.itc.zuulproxy.model.DefaultSettings;
import com.tuneit.itc.zuulproxy.model.Preferences;
import com.tuneit.itc.zuulproxy.repositories.PreferencesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class StartupApplicationListener implements
        ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private PreferencesRepository preferencesRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Custom init start");
        List<Preferences> prefs = preferencesRepository.findAll();
        if (prefs.isEmpty()) {
            Preferences preferences = new Preferences();
            preferences.setAuthEnabled(DefaultSettings.AUTH_ENABLED);
            preferences.setAuthHeader(DefaultSettings.AUTH_HEADER_NAME);
            preferencesRepository.save(preferences);
            log.info("Create default preferences {}", preferences);
        }
        log.info("Custom init complete");
    }
}