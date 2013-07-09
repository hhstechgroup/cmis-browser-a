package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author volodymyr.kozubal <volodymyr.kozubal@engagepoint.com>
 */


@ManagedBean(name = "action")
@SessionScoped
public class ActionBean implements Serializable {

    @ManagedProperty(value = "#{treeBean}")
    private TreeBean treeBean;

    private String type;
    private String reqEx;
    private String name;
    private String newName;
    private UIComponent renamecomponent;
    private UIComponent createcomponent;
    private static Logger logger;
    private final CMISService cmisService;

    /**
     * Handling exception and create a message to show user om dialog page  and log the exception
     * method fail validation and skip all the subsequent phases and go to render response
     * phase to avoid closing dialog with the client
     *
     * @param ex        Exception that is thown from service layer
     * @param component Component to which error is binding
     */

    private void catchException(Exception ex, UIComponent component) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage error_msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "", ex.getMessage());
        FacesContext.getCurrentInstance().addMessage(component.getClientId(), error_msg);
        FacesContext.getCurrentInstance().validationFailed();
        FacesContext.getCurrentInstance().renderResponse();
        logger.log(Level.SEVERE, "Exception: ", ex);
    }

    public ActionBean() {
        logger = Logger.getLogger(ActionBean.class.getName());
        cmisService = CMISServiceImpl.getService();
        reqEx = "^(\\w+\\.?)*\\w+$";
    }

    /**
     * Create new folder with name {@link this#name} and type {@link this#type}
     * in {@link TreeBean#parent} parent directory
     *
     * @param parent folder to set as a parent for new folder
     */
    public void createFolder(TreeNode parent) {
        try {
             if (parent!=null){
            cmisService.createFolder((FSFolder)parent.getData(), name);
            treeBean.updateTree(parent);
             }
            //TODO enable message when node is not selected       kozubal
            //parent not selected
        } catch (Exception ex) {
            catchException(ex, createcomponent);
        }
        this.name = "";
        this.type = "";

    }

    /**
     * Rename folder or file with name {@link TreeBean#selectedFSObject}
     * in {@link TreeBean#parent} parent directory
     *
     * @param fsObject object(file or folder) to rename
     */
    public void rename(FSObject fsObject) {
        try {
            if (fsObject instanceof FSFolder) {
                cmisService.renameFolder((FSFolder) fsObject, newName);

            } else if (fsObject instanceof FSFile) {
                cmisService.renameFile((FSFile) fsObject, newName);
            }
        } catch (Exception ex) {
            catchException(ex,renamecomponent);
        }
        //TODO   java.lang.NullPointerException      kozubal
        treeBean.updateTree(treeBean.getSelectedNode().getParent());
        this.newName = "";
    }

    /**
     * Delete empty folder or folder subtree with name {@link TreeBean#selectedFSObject}
     * in {@link TreeBean#parent} parent directory
     *
     * @param object folder that is supposed to delete
     */
    public void delete(FSObject object) {
        if (object instanceof FSFolder) {
            if (cmisService.hasChildren((FSFolder) object)) {
                RequestContext.getCurrentInstance().execute("confirmationDeleteTree.show()");
            } else {
                cmisService.deleteFolder((FSFolder) object);
                treeBean.updateTree(treeBean.getSelectedNode().getParent());
            }
        } else if (object instanceof FSFile) {
            cmisService.deleteFile((FSFile) object);
            treeBean.updateTree(treeBean.getSelectedNode().getParent());
        }
    }
    /**
     * Delete not empty folder with name {@link TreeBean#selectedFSObject}
     * in {@link TreeBean#parent} parent directory
     *
     * @param object folder that is supposed to delete
     */
    public void deleteAllTree(FSObject object) {
        cmisService.deleteAllTree((FSFolder) object);
        treeBean.updateTree(treeBean.getSelectedNode().getParent());
//        RequestContext.getCurrentInstance().update("treeForm:tree:0");
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


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UIComponent getRenamecomponent() {
        return renamecomponent;
    }

    public void setRenamecomponent(UIComponent renamecomponent) {
        this.renamecomponent = renamecomponent;
    }

    public UIComponent getCreatecomponent() {
        return createcomponent;
    }

    public void setCreatecomponent(UIComponent createcomponent) {
        this.createcomponent = createcomponent;
    }

    public String getReqEx() {
        return reqEx;
    }

    public void setReqEx(String reqEx) {
        this.reqEx = reqEx;
    }

    public TreeBean getTreeBean() {
        return treeBean;
    }

    public void setTreeBean(TreeBean treeBean) {
        this.treeBean = treeBean;
    }
    public void setNewName(String newName) {
        this.newName = newName;
    }
}
