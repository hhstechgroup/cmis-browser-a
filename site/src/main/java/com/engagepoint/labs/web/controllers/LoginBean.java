package com.engagepoint.labs.web.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
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
    private String login;
    private String password;
    private String repository;
    private boolean anonymous;
    private List<SelectItem> repositories = new ArrayList<SelectItem>();

    public LoginBean(){
        anonymous=true;
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
        if (repositories.isEmpty()) {
            repositories.add(new SelectItem("http://repo.opencmis.org/inmemory/atom1/", "Chemistry"));
            repositories.add(new SelectItem("http://repo.opencmis.org/inmemory/atom2/", "EngagePoint"));
        }
        return repositories;
    }
}
