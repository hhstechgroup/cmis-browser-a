package com.engagepoint.labs.web.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

@ManagedBean(name = "config")
@SessionScoped
public class ConfigBean implements Serializable {

    @ManagedProperty(value = "#{login}")
    private LoginBean loginBean;

    private final static String repoURLkey = "REPO_URL";
    private final static String repoNameKey = "REPO_NAME";
    private final static String nameColumnRender = "nameColumnRender";
    private final static String typeIDColumnRender = "typeIDColumnRender";
    private final static String parentTypeIdColumnRender = "parentTypeIdColumnRender";
    private final static String idColumnRender = "idColumnRender";
    private final static String sizeColumnRender = "sizeColumnRender";
    private final static String contentTypeColumnRender = "contentTypeColumnRender";
    private final static String createdByColumnRender = "createdByColumnRender";
    private final static String creationDateColumnRender = "creationDateColumnRender";
    private final static String lastModifiedByColumnRender = "lastModifiedByColumnRender";
    private final static String lastModifiedDateColumnRender = "lastModifiedDateColumnRender";

    private Preferences prefs;
    private String repoURL = "";
    private String repoName = "";
    private Logger logger = Logger.getLogger(ConfigBean.class.getName());

    private boolean name = true;
    private boolean typeId = true;
    private boolean parentTypeId = true;
    private boolean id = true;
    private boolean size = true;
    private boolean contentType = true;
    private boolean createdBy = true;
    private boolean creationDate = true;
    private boolean lastModifiedBy = true;
    private boolean lastModifiedDate = true;

    private List<SelectItem> repositories;

    public ConfigBean() {
        repositories = new LinkedList<SelectItem>();
        prefs = Preferences.userRoot().node(this.getClass().getName());
    }

    public void savePrefs() {
        prefs.putBoolean(nameColumnRender, name);
        prefs.putBoolean(typeIDColumnRender, typeId);
        prefs.putBoolean(parentTypeIdColumnRender, parentTypeId);
        prefs.putBoolean(idColumnRender, id);
        prefs.putBoolean(contentTypeColumnRender, contentType);
        prefs.putBoolean(createdByColumnRender, createdBy);
        prefs.putBoolean(creationDateColumnRender, creationDate);
        prefs.putBoolean(sizeColumnRender, size);
        prefs.putBoolean(lastModifiedByColumnRender, lastModifiedBy);
        prefs.putBoolean(lastModifiedDateColumnRender, lastModifiedDate);
        prefs.put(repoURLkey, repoURL);
        prefs.put(repoNameKey, repoName);
        repositories.add(new SelectItem(repoURL, repoName));

        loginBean.setRepositories(repositories);
        try {
            prefs.exportSubtree(new BufferedOutputStream(new FileOutputStream("D:\\settings.properties")));
        } catch (IOException e) {
        } catch (BackingStoreException e) {
        }
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        } catch (IOException e) {
        }
    }

    public String getRepoName() {
        return prefs.get(repoNameKey, "");
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoURL() {
        return prefs.get(repoURLkey, "");
    }

    public void setRepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public boolean isName() {
        return prefs.getBoolean(nameColumnRender, true);
    }

    public void setName(boolean name) {
        this.name = name;
    }

    public boolean isTypeId() {
        return prefs.getBoolean(typeIDColumnRender, true);
    }

    public void setTypeId(boolean typeId) {
        this.typeId = typeId;
    }


    public boolean isParentTypeId() {
        return prefs.getBoolean(parentTypeIdColumnRender, true);
    }

    public void setParentTypeId(boolean parentTypeId) {
        this.parentTypeId = parentTypeId;
    }

    public boolean isId() {
        return prefs.getBoolean(idColumnRender, true);
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public boolean isSize() {
        return prefs.getBoolean(sizeColumnRender, true);
    }

    public void setSize(boolean size) {
        this.size = size;
    }

    public boolean isContentType() {
        return prefs.getBoolean(contentTypeColumnRender, true);
    }

    public void setContentType(boolean contentType) {
        this.contentType = contentType;
    }

    public boolean isCreatedBy() {
        return prefs.getBoolean(createdByColumnRender, true);
    }

    public void setCreatedBy(boolean createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isLastModifiedBy() {
        return prefs.getBoolean(lastModifiedByColumnRender, true);
    }

    public void setLastModifiedBy(boolean lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public boolean isLastModifiedDate() {
        return prefs.getBoolean(lastModifiedDateColumnRender, true);
    }

    public void setLastModifiedDate(boolean lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public boolean isCreationDate() {
        return prefs.getBoolean(creationDateColumnRender, true);
    }

    public void setCreationDate(boolean creationDate) {
        this.creationDate = creationDate;
    }

    public LoginBean getLoginBean() {
        return loginBean;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }
}