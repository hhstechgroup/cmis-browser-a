package com.engagepoint.labs.core.models.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: r.reznichenko
 * Date: 7/17/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileAlreadyExistException extends BaseException {

    public FileAlreadyExistException() {
    }

    public FileAlreadyExistException(String message) {
        super(message);
    }

    public FileAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileAlreadyExistException(Throwable cause) {
        super(cause);
    }
}
