package com.tuneit.itc.zuulproxy.repositories;

import com.tuneit.itc.zuulproxy.model.Preferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferencesRepository extends JpaRepository<Preferences, Long> {

}
