package com.tuneit.itc.feedback;

import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;
import com.liferay.faces.portal.el.internal.Liferay;
import com.liferay.faces.util.i18n.internal.I18nImpl;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailServiceUtil;
import com.liferay.portal.kernel.model.User;

import com.tuneit.itc.commons.model.Feedback;
import com.tuneit.itc.commons.service.FeedbackService;
import com.tuneit.itc.commons.service.PortletPreferencesService;

/**
 * Feedback sending logic.
 *
 * @author Alexander Pashnin
 */

@Data
@ViewScoped
@ManagedBean
public class FeedbackSenderBean {
    public static final String PORTLET_CONFIGURATION_KEY = "FEEDBACK_PORTLET_CONFIG";
    private static final String SUBJECT_TEMPLATE = "Отзыв о работе портала №%s от %s";
    private static final String BODY_TEMPLATE = "Имя: %s\n" +
        "№ обращения: %s\n" +
        "Email: %s\n" +
        "Сообщение:\n" +
        "%s";
    Logger log = LoggerFactory.getLogger(FeedbackSenderBean.class);
    private String name;
    private String email;
    private String text;
    private User user;
    private Mode mode;
    private boolean preferencesLoaded;
    //Configuration
    private String emailFrom;
    private List<String> emailsTo;
    private ObjectMapper mapper = new ObjectMapper();
    @ManagedProperty("#{liferay}")
    private Liferay liferay;
    @ManagedProperty("#{feedbackService}")
    private FeedbackService feedbackService;
    @ManagedProperty("#{portletPreferencesService}")
    private PortletPreferencesService preferencesService;

    @PostConstruct
    public void init() {
        user = liferay.getThemeDisplay().getUser();
        resetAll();
        loadConfig();
    }

    /**
     * Saves feedback message into DB with user associating.
     * If user is anonymous, message won't be associated with anybody.
     * Sends feedback message from/to email addresses which were set during configuration.
     */
    public void sendFeedback() {
        InternetAddress fromAddress = null;
        List<InternetAddress> toAddresses = new ArrayList<>();

        var feedback = new Feedback();
        feedback.setEmail(email);
        feedback.setName(name);
        feedback.setText(text);
        feedback.setUserId(user.getDefaultUser() ? null : user.getUserId());
        Feedback savedFeedback = feedbackService.save(feedback);
        try {
            fromAddress = new InternetAddress(emailFrom);
            for (String e : emailsTo) {
                toAddresses.add(new InternetAddress(e));
            }
            MailMessage mailMessage = new MailMessage();
            mailMessage.setTo(toAddresses.toArray(new InternetAddress[0]));
            mailMessage.setFrom(fromAddress);
            mailMessage.setSubject(String.format(SUBJECT_TEMPLATE, savedFeedback.getId(), email));
            String body = String.format(BODY_TEMPLATE, name, savedFeedback.getId(), email, text);
            mailMessage.setBody(body);
            MailServiceUtil.sendEmail(mailMessage);
            log.info(String.format("Send feedback №%s from name: %s, email: %s", savedFeedback.getId(), name, email));
            FacesMessage successMessage = getLocalized("feedback.success", FacesMessage.SEVERITY_INFO);
            successMessage.setDetail(String.valueOf(savedFeedback.getId()));
            FacesContext.getCurrentInstance().addMessage(null, successMessage);
        } catch (AddressException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                getLocalized("feedback.error", FacesMessage.SEVERITY_ERROR));
            log.error("Address exception! " + e.getMessage());
            log.debug("An exception occurred", e);
        }
        resetAll();
    }

    public boolean isAdmin() {
        return liferay.getThemeDisplay().getPermissionChecker().isOmniadmin();
    }

    public boolean isAnonymous() {
        return user.getDefaultUser();
    }

    private void resetAll() {
        name = null;
        email = null;
        text = null;
        mode = Mode.VIEW;
        if (!user.getDefaultUser()) {
            name = user.getFullName();
            email = user.getEmailAddress();
        }
    }

    public void resetPreferences() {
        mode = Mode.VIEW;
        loadConfig();
    }

    public boolean isPreferencesLoaded() {
        return preferencesLoaded;
    }

    private void loadConfig() {
        preferencesLoaded = false;
        var prefOpt = preferencesService.find(PORTLET_CONFIGURATION_KEY);
        if (prefOpt.isPresent()) {
            var prefs = prefOpt.get();
            try {
                FeedbackPortletConfig config = mapper.readValue(prefs.getValue(), FeedbackPortletConfig.class);
                emailFrom = config.getEmailFrom();
                emailsTo = config.getEmailsTo();
            } catch (IOException e) {
                log.error("Unexpected exception: ", e);
            }
            preferencesLoaded = true;
        }
    }

    /**
     * Save given configuration of the portlet to DB. Perform email validation.
     */
    public void savePreferences() {
        List<String> invalidEmails = new ArrayList<>();
        if (!emailIsValid(emailFrom)) {
            invalidEmails.add(emailFrom);
        }

        for (String e : emailsTo) {
            if (!emailIsValid(e)) {
                invalidEmails.add(e);
            }
        }

        if (invalidEmails.size() > 0) {
            FacesMessage emailError = getLocalized("feedback.preferences.emails.incorrect",
                FacesMessage.SEVERITY_ERROR);
            emailError.setDetail(invalidEmails.toString());
            FacesContext.getCurrentInstance().addMessage(null, emailError);
            return;
        }

        try {
            var prefOpt = preferencesService.save(PORTLET_CONFIGURATION_KEY,
                new FeedbackPortletConfig(emailFrom, emailsTo));
        } catch (Exception e) {
            log.error(e);
            FacesContext.getCurrentInstance().addMessage(null, getLocalized("feedback.preferences.error",
                FacesMessage.SEVERITY_ERROR));
            return;
        }
        loadConfig();
        setModeView();
    }

    private boolean emailIsValid(String email) {
        try {
            var ia = new InternetAddress(email);
            ia.validate();
        } catch (AddressException e) {
            return false;
        }
        return true;
    }

    private FacesMessage getLocalized(String key, FacesMessage.Severity severity) {
        I18nImpl i18n = new I18nImpl();
        return i18n.getFacesMessage(FacesContext.getCurrentInstance(),
            LiferayPortletHelperUtil.getThemeDisplay().getLocale(), severity, key);
    }

    public boolean isModeEdit() {
        return mode == Mode.EDIT;
    }

    public boolean isModeView() {
        return mode == Mode.VIEW;
    }

    public void setModeEdit() {
        mode = Mode.EDIT;
    }

    public void setModeView() {
        mode = Mode.VIEW;
    }

    private enum Mode {
        VIEW,
        EDIT
    }

}
