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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FSFile fsFile = (FSFile) o;

        if (versionable != fsFile.versionable) return false;
        if (absolutePath != null ? !absolutePath.equals(fsFile.absolutePath) : fsFile.absolutePath != null)
            return false;
        if (allVersions != null ? !allVersions.equals(fsFile.allVersions) : fsFile.allVersions != null) return false;
        if (versionLabel != null ? !versionLabel.equals(fsFile.versionLabel) : fsFile.versionLabel != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (absolutePath != null ? absolutePath.hashCode() : 0);
        result = 31 * result + (versionLabel != null ? versionLabel.hashCode() : 0);
        result = 31 * result + (versionable ? 1 : 0);
        result = 31 * result + (allVersions != null ? allVersions.hashCode() : 0);
        return result;
    }
}
