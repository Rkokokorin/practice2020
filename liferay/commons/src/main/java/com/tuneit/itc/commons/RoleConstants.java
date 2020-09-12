package com.tuneit.itc.commons;

public class RoleConstants {

    public static final String CONTRACTOR_ADMIN = "com.tuneit.itc.contractor.admin";
    public static final String CONTRACTOR_ADMIN_GLOBAL = "com.tuneit.itc.contractor.admin.global";
    public static final String CONTRACTOR_EMPLOYEE = "com.tuneit.itc.contractor.employee";
    public static final String CONTRACTORS_MANAGER = "com.tuneit.itc.portal.contractor_manager";
    public static final String SALES_MANAGER = "com.tuneit.itc.portal.sales_manager";
    public static final String SALES_DEPARTMENT_HEAD = "com.tuneit.itc.portal.sales_manager_head";
    public static final String WEB_CONTENT_EDITOR = "com.tuneit.itc.portal.web.content.editor";


    private RoleConstants() {
        throw new IllegalStateException("This is an utility class!");
    }
}
