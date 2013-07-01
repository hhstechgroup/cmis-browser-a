package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.SelectEvent;

import javax.faces.bean.*;
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
    private String newName;
    private boolean deleteAllTree = false;

    private static Logger logger = Logger.getLogger(ActionBean.class.getName());

    private final CMISService service = CMISServiceImpl.getService();

    public void createFolder(FSFolder parent) {
        logger.log(Level.INFO, "I'm in create folder, parent: "+parent.getPath()+" and name:"+name);
        service.createFolder(parent, name);
        this.name = "";
    }

    public void rename(FSObject fsObject) {
        logger.log(Level.INFO, "RENAMEEEEEEEEEE");
        logger.log(Level.INFO, "fsObject null? - " + (fsObject == null));
        if(fsObject instanceof FSFolder) {
            logger.log(Level.INFO, "I'm renaming folder: " + fsObject.getName());
            service.renameFolder((FSFolder)fsObject, newName);
        } else if(fsObject instanceof FSFile) {
            logger.log(Level.INFO, "I'm renaming file: " + fsObject.getName());
            service.renameFile((FSFile)fsObject, newName);
        }
        this.newName = "";
    }

    public void delete(FSFolder folder) {
        logger.log(Level.INFO, "DELEEEETEEEEEEEEEE");
        if(deleteAllTree) {
            deleteAllTree = false;
            service.deleteAllTree(folder);
        } else {
            service.deleteFolder(folder);
        }
    }

    public String getName() {
        logger.log(Level.INFO, "getName: "+name);
        return name;
    }

    public void setName(String name) {
        logger.log(Level.INFO, "setName: "+name);
        this.name = name;
    }

    public String getNewName() {
        logger.log(Level.INFO, "getNewName: "+newName);
        return newName;
    }

    public void setNewName(String newName) {
        logger.log(Level.INFO, "setNewName: "+newName);
        this.newName = newName;
    }

    public boolean isDeleteAllTree() {
        return deleteAllTree;
    }

    public void setDeleteAllTree(boolean deleteAllTree) {
        this.deleteAllTree = deleteAllTree;
    }
}
