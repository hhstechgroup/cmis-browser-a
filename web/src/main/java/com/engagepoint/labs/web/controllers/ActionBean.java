package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.logging.Level;
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
    private boolean deleteAllTree = false;
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private final CMISService service = CMISServiceImpl.getService();

    public FSFolder createFolder(FSFolder parent) {
        return service.createFolder(parent, name);
    }

    public FSObject rename(FSObject fsObject) {
        if(fsObject instanceof FSFolder) {
            return service.renameFolder((FSFolder)fsObject, name);
        } else if(fsObject instanceof FSFile) {
            return service.renameFile((FSFile)fsObject, name);
        }
        //for future, when will not only documents and folders
        return null;
    }

    public boolean delete(FSFolder folder) {
        if(deleteAllTree) {
            deleteAllTree = false;
            return service.deleteAllTree(folder);
        }
        return service.deleteFolder(folder);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeleteAllTree() {
        return deleteAllTree;
    }

    public void setDeleteAllTree(boolean deleteAllTree) {
        this.deleteAllTree = deleteAllTree;
    }
}
