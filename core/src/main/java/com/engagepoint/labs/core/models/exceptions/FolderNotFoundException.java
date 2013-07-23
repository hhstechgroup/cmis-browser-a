package com.engagepoint.labs.core.models.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 7/23/13
 * Time: 10:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class FolderNotFoundException extends BaseException{
    public FolderNotFoundException() {
    }

    public FolderNotFoundException(String message) {
        super(message);
    }

    public FolderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FolderNotFoundException(Throwable cause) {
        super(cause);
    }
}
