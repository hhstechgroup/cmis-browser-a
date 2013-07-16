package com.engagepoint.labs.core.models;

/**
 * User: vitaliy.vasilenko
 * Date: 6/17/13
 * Time: 2:51 PM
 */
public class FSFile extends FSObject {

    private String absolutePath;

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    @Override
    public String getIcon(){
        return "document.png";
    }
}
