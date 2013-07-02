package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Rostik
 * Date: 25.06.13
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean(name = "action")
@ApplicationScoped
public class ActionBean implements Serializable {

    private String name;
    private String newName;
    private boolean deleteAllTree = false;

    private static Logger logger = Logger.getLogger(ActionBean.class.getName());

    private final CMISService service = CMISServiceImpl.getService();

    public void createFolder(FSFolder parent) {
        service.createFolder(parent, name);
        this.name = "";
    }

    public void rename(FSObject fsObject) {
        if (fsObject instanceof FSFolder) {
            service.renameFolder((FSFolder) fsObject, newName);
        } else if (fsObject instanceof FSFile) {
            service.renameFile((FSFile) fsObject, newName);
        }
        this.newName = "";
    }

    public void delete(FSFolder folder) {
        if (deleteAllTree) {
            deleteAllTree = false;
            service.deleteAllTree(folder);
        } else {
            service.deleteFolder(folder);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public boolean isDeleteAllTree() {
        return deleteAllTree;
    }

    public void setDeleteAllTree(boolean deleteAllTree) {
        this.deleteAllTree = deleteAllTree;
    }
}
