package com.tuneit.itc.jsf;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

public class LiferayJsfExceptionLogger extends ExceptionHandlerWrapper {
    private static final Log log = LogFactoryUtil.getLog(LiferayJsfExceptionLogger.class);
    private ExceptionHandler wrapped;

    public LiferayJsfExceptionLogger(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {

        for (ExceptionQueuedEvent event : getUnhandledExceptionQueuedEvents()) {
            Throwable throwable = event.getContext().getException();
            if (throwable instanceof ViewExpiredException) {
                log.trace("View expired! " + throwable.getMessage() + " "
                    + ((ViewExpiredException) throwable).getViewId());
                try {
                    FacesContext.getCurrentInstance().getExternalContext().responseSendError(500, "");
                } catch (IOException ioe) {
                    log.error(ioe);
                }
                return; // Suppress gibibytes of this logs
            }
            log.error(throwable);
        }
        super.handle();
    }
}
