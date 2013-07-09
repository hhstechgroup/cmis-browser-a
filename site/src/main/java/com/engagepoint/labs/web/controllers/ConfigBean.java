package com.engagepoint.labs.web.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;

@ManagedBean
@SessionScoped
public class ConfigBean implements Serializable {
    @ManagedProperty(value = "#{treeBean}")
    private TreeBean treeBean;
    private String name;
    private String currentFolderName;
    private String configTitle = "Test";

    public ConfigBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCurrentFolderName(String currentFolderName) {
        this.currentFolderName = currentFolderName;
    }

    public String getCurrentFolderName() {
        return currentFolderName;
    }

    public TreeBean getTreeBean() {
        return treeBean;
    }

    public void setTreeBean(TreeBean treeBean) {
        this.treeBean = treeBean;
    }

    public String getConfigTitle() {
        return configTitle;
        //+""+getTreeBean().getSelectedFSObject().getName()
    }

    public void setConfigTitle(String configTitle) {
        this.configTitle = configTitle;
    }
}