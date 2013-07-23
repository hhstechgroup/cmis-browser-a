package com.engagepoint.labs.web.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

@ManagedBean(name = "config")
@SessionScoped
public class ConfigBean implements Serializable {

    private final static String repoURLkey = "REPO_URL";
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
    private String repoURL;
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

    public ConfigBean() {
        prefs = Preferences.userRoot();
    }

    public void savePrefs() {
        prefs.put(repoURLkey, repoURL);
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
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
        } catch (IOException e) {
        }
    }

    public String getRepoURL() {
        return repoURL;
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
}