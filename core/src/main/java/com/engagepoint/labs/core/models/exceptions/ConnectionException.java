package com.engagepoint.labs.core.models.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 7/17/13
 * Time: 6:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionException extends BaseException {
    public ConnectionException() {
    }

    public ConnectionException(String message) {
        super(message);
    }

    ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    ConnectionException(Throwable cause) {
        super(cause);
    }
}
