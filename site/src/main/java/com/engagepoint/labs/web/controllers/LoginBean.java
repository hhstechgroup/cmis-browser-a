package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 6/17/13
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
@ManagedBean(name = "login")
@SessionScoped
public class LoginBean implements Serializable {

    @ManagedProperty(value = "#{treeBean}")
    private TreeBean treeBean;

    private String login;
    private String password;
    private String repository;
    private boolean anonymous;
    private List<SelectItem> repositories = new ArrayList<SelectItem>();

    private CMISService cmisService;

    public LoginBean() {
        anonymous = true;
        try {
            cmisService = CMISServiceImpl.getService();
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
    }

    public void doLogin() {
        try {
            if (anonymous) {
                login = "";
                password = "";
            }
            cmisService.connect(login, password, repository);
            treeBean.drawComponent();
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(), ""));
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public void changeAnonumous() {
        this.anonymous = !anonymous;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public List<SelectItem> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<SelectItem> repositories) {
        this.repositories = repositories;
    }

    public TreeBean getTreeBean() {
        return treeBean;
    }

    public void setTreeBean(TreeBean treeBean) {
        this.treeBean = treeBean;
    }
}
