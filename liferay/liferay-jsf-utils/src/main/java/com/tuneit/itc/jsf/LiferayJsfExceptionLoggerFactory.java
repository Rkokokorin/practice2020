package com.tuneit.itc.jsf;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class LiferayJsfExceptionLoggerFactory extends ExceptionHandlerFactory {
    private final ExceptionHandlerFactory parent;

    public LiferayJsfExceptionLoggerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new LiferayJsfExceptionLogger(parent.getExceptionHandler());
    }
}
