package com.engagepoint.labs.core.models.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 7/19/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class BrowserRuntimeException extends BaseException {
    public BrowserRuntimeException() {
    }

    public BrowserRuntimeException(String message) {
        super(message);
    }

    public BrowserRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrowserRuntimeException(Throwable cause) {
        super(cause);
    }
}
