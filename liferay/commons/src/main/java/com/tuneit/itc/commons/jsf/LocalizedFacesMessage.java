package com.tuneit.itc.commons.jsf;

import java.util.IllegalFormatException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import com.liferay.faces.util.el.internal.I18nMap;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class LocalizedFacesMessage {

    private LocalizedFacesMessage() {
        throw new IllegalStateException("This is an utility class");
    }

    /**
     * Writes info-level message without description into.
     *
     * @param summary Message to write
     */
    public static void info(String summary) {
        info(summary, null);
    }

    /**
     * Writes info message with description into FacesContext.
     *
     * @param summary Message
     * @param detail  Message description
     */
    public static void info(String summary, String detail) {
        info(null, summary, detail);
    }

    /**
     * Writes info-level message for specified client.
     *
     * @param clientId Client id for message binding
     * @param summary  Message
     * @param detail   Message description
     */
    public static void info(String clientId, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(clientId,
            new FacesMessage(FacesMessage.SEVERITY_INFO, i18n(summary), i18n(detail)));
    }

    public static void infoFormat(String summaryKey, Object[] summaryArgs) {
        infoFormat(null, summaryKey, summaryArgs, null, null);
    }

    public static void infoFormat(String summaryKey, Object[] summaryArgs, String detailKey, Object[] detailArgs) {
        infoFormat(null, summaryKey, summaryArgs, detailKey, detailArgs);
    }

    public static void infoFormat(String clientId, String summaryKey, Object[] summaryArgs,
                                  String detailKey, Object[] detailArgs) {
        String summary = tryFormat(summaryKey, summaryArgs);
        String detail = tryFormat(detailKey, detailArgs);
        facesMessage(FacesMessage.SEVERITY_INFO, clientId, summary, detail);
    }

    public static void errorFormat(String summaryKey, Object[] summaryArgs) {
        errorFormat(null, summaryKey, summaryArgs, null, null);
    }

    public static void errorFormat(String summaryKey, Object[] summaryArgs, String detailKey, Object[] detailArgs) {
        errorFormat(null, summaryKey, summaryArgs, detailKey, detailArgs);
    }

    public static void errorFormat(String clientId, String summaryKey, Object[] summaryArgs,
                                   String detailKey, Object[] detailArgs) {
        String summary = tryFormat(summaryKey, summaryArgs);
        String detail = tryFormat(detailKey, detailArgs);
        facesMessage(FacesMessage.SEVERITY_ERROR, clientId, summary, detail);
    }

    private static void facesMessage(FacesMessage.Severity severity, String clientId, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(clientId,
            new FacesMessage(severity, summary, detail));
    }

    private static String tryFormat(String key, Object[] args) {
        if (key == null) {
            return null;
        }
        String value = i18n(key);
        if (args != null && args.length > 0) {
            try {
                value = String.format(value, args);
            } catch (IllegalFormatException exc) {
                LogFactoryUtil.getLog(LocalizedFacesMessage.class).warn("Can not format message. Key: "
                    + key + ", value: " + value);
            }
        }
        return value;
    }

    /**
     * Writes error-level message without description into FacesContext.
     *
     * @param summary Message to write
     */
    public static void error(String summary) {
        error(summary, null);
    }

    /**
     * Writes error message with description into FacesContext.
     *
     * @param summary Message
     * @param detail  Message description
     */
    public static void error(String summary, String detail) {
        error(null, summary, detail);
    }

    /**
     * Writes error-level message for specified client.
     *
     * @param clientId Client id for message binding
     * @param summary  Message
     * @param detail   Message description
     */
    public static void error(String clientId, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(clientId,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, i18n(summary), i18n(detail)));
    }

    /**
     * Writes warning-level message without description into FacesContext.
     *
     * @param summary Message to write
     */
    public static void warn(String summary) {
        warn(summary, null);
    }

    /**
     * Writes warning message with description into FacesContext.
     *
     * @param summary Message
     * @param detail  Message description
     */
    public static void warn(String summary, String detail) {
        warn(null, summary, detail);
    }

    /**
     * Writes warn-level message for specified client.
     *
     * @param clientId Client id for message binding
     * @param summary  Message
     * @param detail   Message description
     */
    public static void warn(String clientId, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(clientId,
            new FacesMessage(FacesMessage.SEVERITY_WARN, i18n(summary), i18n(detail)));
    }

    /**
     * Writes fatal error message without description into FacesContext.
     *
     * @param summary Message to write
     */
    public static void fatal(String summary) {
        fatal(summary, null);
    }

    /**
     * Writes fatal error message with description into FacesContext.
     *
     * @param summary Message
     * @param detail  Message description
     */
    public static void fatal(String summary, String detail) {
        fatal(null, summary, detail);
    }

    /**
     * Writes fatal-level message for specified client.
     *
     * @param clientId Client id for message binding
     * @param summary  Message
     * @param detail   Message description
     */
    public static void fatal(String clientId, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(clientId,
            new FacesMessage(FacesMessage.SEVERITY_INFO, i18n(summary), i18n(detail)));
    }

    public static String i18n(String key) {
        if (key == null) {
            return null;
        }
        return (String) new I18nMap().get(key);
    }
}
