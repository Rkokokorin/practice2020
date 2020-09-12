package com.tuneit.itc.commons.util;

import com.liferay.portal.kernel.util.PropsUtil;

/**
 * Base class for accessing Liferay properties.
 *
 * @author kk
 */
public class Properties {
    public static String getOrDefault(String name, String def) {
        String val = PropsUtil.get(name);
        return null == val ? def : val;
    }

    public static int getOrDefault(String name, int def) {
        String val = PropsUtil.get(name);
        return null == val ? def : Integer.parseInt(val);
    }

}
