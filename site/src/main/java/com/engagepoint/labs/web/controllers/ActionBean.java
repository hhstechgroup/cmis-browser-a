package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;

import javax.faces.application.FacesMessage;
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
    @ManagedProperty(value = "#{fileActions}")
    private FileActions fileActions;

    private String type;
    private String reqEx;
    private String name;
    private String defaultFolderName = "Copy_";
    private UIComponent renamecomponent;
    private UIComponent createcomponent;
    private static Logger logger;
    private final CMISService cmisService;

    private String uploadVisible = "hidden";  // for edit dialog

    private FSObject folderForCopy;

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

    public void copyFolder(FSObject target) {
        try {
            if (!folderForCopy.getName().equals(defaultFolderName)) {
                folderForCopy.setName(defaultFolderName);
            }
            cmisService.copyFolder(folderForCopy.getId(), folderForCopy.getName(), target.getId());
        } catch (NullPointerException ex) {

        }

    }

    public ActionBean() {
        logger = Logger.getLogger(ActionBean.class.getName());
        cmisService = CMISServiceImpl.getService();
        reqEx = "(.*[\\\\\\/]|^)(.*?)(?:[\\.]|$)([^\\.\\s]*$)";
    }

    public void markFolder(FSObject folderForCopy) {
        logger.log(Level.INFO, "folderForCopy: " + folderForCopy.getName());
        this.folderForCopy = folderForCopy;
    }

    public void createFile(FSFolder parent) {
        cmisService.createFile(parent, name, fileActions.getFile().getContents(), fileActions.getFile().getContentType());
        treeBean.updatetablePageList();
    }

    /**
     * Create new folder with name {@link this#name} and type {@link this#type}
     * in {@link TreeBean#parent} parent directory
     *
     * @param parent folder to set as a parent for new folder
     */
    public void createFolder(TreeNode parent) {
        try {
            if (parent != null) {
                cmisService.createFolder((FSFolder) parent.getData(), name);
                treeBean.updateTree(parent);
            }
            //TODO enable message when node is not selected       kozubal
            //parent not selected
        } catch (Exception ex) {
            catchException(ex, createcomponent);
        }
        this.name = "";
        this.type = "";
        treeBean.updatetablePageList();
    }

    public void edit(FSObject selected) {
        if (!selected.getName().equals(fileActions.getSelectedName())) {
            rename(selected);
        }
        if (selected instanceof FSFile) {
            UploadedFile file = fileActions.getFile();
            if (file != null) {
                byte[] content = file.getContents();
                String mimeType = file.getContentType();
                cmisService.edit((FSFile) selected, content, mimeType);
            }
        }
    }

    /**
     * Delete empty folder or folder subtree with name {@link TreeBean#selectedFSObject}
     * in {@link TreeBean#parent} parent directory
     *
     * @param object folder that is supposed to delete
     */
    public void delete(FSObject object) {
        if (object instanceof FSFolder) {
            deleteAllTree(object);
        } else {
            cmisService.deleteFile((FSFile) object);
        }
        treeBean.updatetablePageList();
    }

    /**
     * Delete not empty folder with name {@link TreeBean#selectedFSObject}
     * in {@link TreeBean#parent} parent directory
     *
     * @param object folder that is supposed to delete
     */
    public void deleteAllTree(FSObject object) {
        cmisService.deleteAllTree((FSFolder) object);
//        treeBean.updateTree(treeBean.getSelectedNode().getParent());
    }

    /**
     * Rename folder or file with name {@link TreeBean#selectedFSObject}
     * in {@link TreeBean#parent} parent directory
     *
     * @param fsObject object(file or folder) to rename
     */
    private void rename(FSObject fsObject) {
        try {
            if (fsObject instanceof FSFolder) {
                cmisService.renameFolder((FSFolder) fsObject, fsObject.getName());

            } else if (fsObject instanceof FSFile) {
                cmisService.renameFile((FSFile) fsObject, fsObject.getName());
            }
        } catch (Exception ex) {
            catchException(ex, renamecomponent);
        }
        //TODO   java.lang.NullPointerException      kozubal
       /* if (treeBean.getSelectedNode().getParent() != null) {
            treeBean.updateTree(treeBean.getSelectedNode().getParent());
        }*/
    }

    public String getUploadVisible() {
        uploadVisible = fileActions.isSelectedIsFile() ? "visible" : "hidden";
        return uploadVisible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public FileActions getFileActions() {
        return fileActions;
    }

    public void setFileActions(FileActions fileActions) {
        this.fileActions = fileActions;
    }

    public FSObject getFolderForCopy() {
        return folderForCopy;
    }

    public void setFolderForCopy(FSObject folderForCopy) {

        this.folderForCopy = folderForCopy;
    }

    public String getDefaultFolderName() {
        if (folderForCopy != null) {
            defaultFolderName += folderForCopy.getName();
        }
        return defaultFolderName;
    }

    public void setDefaultFolderName(String defaultFolderName) {
        this.defaultFolderName = defaultFolderName;
    }
}
