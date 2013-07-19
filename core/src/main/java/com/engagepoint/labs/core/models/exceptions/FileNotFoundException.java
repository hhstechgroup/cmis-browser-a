package com.engagepoint.labs.core.models.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 7/18/13
 * Time: 5:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileNotFoundException extends BaseException {
    public FileNotFoundException() {
    }

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundException(Throwable cause) {
        super(cause);
    }
}
