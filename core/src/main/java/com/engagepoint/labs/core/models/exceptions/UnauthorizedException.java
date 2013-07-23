package com.engagepoint.labs.core.models.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 7/19/13
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class UnauthorizedException extends BaseException {
    public UnauthorizedException() {
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}
