package com.engagepoint.labs.core.models.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: r.reznichenko
 * Date: 7/17/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class FolderAlreadyExistException extends BaseException {

    public FolderAlreadyExistException() {
    }

    public FolderAlreadyExistException(String message) {
        super(message);
    }

}
