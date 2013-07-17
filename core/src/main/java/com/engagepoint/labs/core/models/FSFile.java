package com.engagepoint.labs.core.models;

import java.util.List;

/**
 * User: vitaliy.vasilenko
 * Date: 6/17/13
 * Time: 2:51 PM
 */
public class FSFile extends FSObject {

    private String absolutePath;
    private String versionLabel;
    private boolean versionable;
    private List<FSFile> allVersions;

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

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public List<FSFile> getAllVersions() {
        return allVersions;
    }

    public void setAllVersions(List<FSFile> allVersions) {
        this.allVersions = allVersions;
    }

    public boolean isVersionable() {
        return versionable;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
    }
}
