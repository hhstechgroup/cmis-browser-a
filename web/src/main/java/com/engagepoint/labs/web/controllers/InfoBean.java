package com.engagepoint.labs.web.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 6/18/13
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
@ManagedBean
@SessionScoped
public class InfoBean {
    private String parrentFolder;

    public String getParrentFolder() {
        return parrentFolder;
    }

    public void setParrentFolder(String parrentFolder) {
        this.parrentFolder = parrentFolder;
    }
}
