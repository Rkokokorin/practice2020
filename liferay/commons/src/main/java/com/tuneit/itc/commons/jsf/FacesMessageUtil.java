package com.tuneit.itc.commons.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public final class FacesMessageUtil {

    private FacesMessageUtil() {
        throw new IllegalStateException("This is an utility class");
    }

    /**
     * Writes info-level message without description into FacesContext.
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
            new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
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
            new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
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
            new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail));
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
            new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }
}

