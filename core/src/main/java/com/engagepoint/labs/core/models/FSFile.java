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
    private List<FSFile> allVersions;
//    System.out.println("\tname: " + version.getName());
//    System.out.println("\tversion label: " + version.getVersionLabel());
//    System.out.println("\tversion series id: " + version.getVersionSeriesId());
//    System.out.println("\tchecked out by: "
//            + version.getVersionSeriesCheckedOutBy());
//    System.out.println("\tchecked out id: "
//            + version.getVersionSeriesCheckedOutId());
//    System.out.println("\tmajor version: " + version.isMajorVersion());
//    System.out.println("\tlatest version: " + version.isLatestVersion());
//    System.out.println("\tlatest major version: " + version.isLatestMajorVersion());
//    System.out.println("\tcheckin comment: " + version.getCheckinComment());
//    System.out.println("\tcontent length: " + version.getContentStreamLength()
//            + "\n");

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
}
