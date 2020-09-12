package com.tuneit.itc.commons.util;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.liferay.portal.kernel.model.User;

import com.tuneit.itc.commons.model.ExtendedUser;

@ManagedBean(eager = true)
@ApplicationScoped
public class UserColumnsInitializer {
    @PostConstruct
    public void init() {
        ExpandoColumnInitializer.initializeExpandoTable(User.class,
            Arrays.asList(ExtendedUser.CURRENCY_CODE, ExtendedUser.WAREHOUSE_CODE));
    }
}
