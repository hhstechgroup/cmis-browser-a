package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.*;
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
import java.util.List;
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
    private CMISService cmisService;
    private List<FSFile> history;
    private FSObject folderForCopy;
    private String findQuery;

    private boolean copyItemPressed = false;

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

    public void copyFolder() {
        FSObject target = treeBean.getSelectedFSObject();
        String tempName = defaultFolderName;
        if (folderForCopy instanceof FSFile) {
            try {
                logger.log(Level.INFO, "file name: " + folderForCopy.getName() + " def name: " + defaultFolderName + " def name: " + tempName);
                try {
                    logger.log(Level.INFO, "file name: " + folderForCopy.getName() + " def name: " + defaultFolderName + " def name: " + tempName);
                    cmisService.copyFile(folderForCopy.getId(), tempName, target.getId());
                } catch (FileAlreadyExistException e) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(),
                            "Rename file, please!"));
                } catch (BaseException e) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(),
                            ""));
                }
            } catch (NullPointerException ex) {

            }
        }

        if (folderForCopy instanceof FSFolder) {
            try {
                logger.log(Level.INFO, "folder name: " + folderForCopy.getName() + " def name: " + defaultFolderName);
                if (defaultFolderName.equals("Copy_")) {
                    tempName = getDefaultFolderName();
                }
                try {
                    cmisService.copyFolder((FSFolder) folderForCopy, tempName, target.getId());
                } catch (FolderAlreadyExistException e) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage(),
                            "Rename folder, please!"));
                }
                finally {
                    logger.log(Level.INFO, "cached node: " + ((FSFolder)treeBean.getCachedNode().getData()).getPath());
                    treeBean.updateTree(treeBean.getCachedNode());
                }
            } catch (NullPointerException ex) {

            }
        }
        defaultFolderName = "Copy_";
        this.copyItemPressed = false;
    }

    public ActionBean() {
        logger = Logger.getLogger(ActionBean.class.getName());
        try {
            cmisService = CMISServiceImpl.getService();
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "catched ActionBean constructor: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "PrimeFaces makes no mistakes"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        reqEx = "(.*[\\\\\\/]|^)(.*?)(?:[\\.]|$)([^\\.\\s]*$)";
    }

    public void markFolder() {
        this.folderForCopy = treeBean.getSelectedFSObject();
        this.copyItemPressed = true;
    }

    public void createFile(FSFolder parent) {
        try {
            cmisService.createFile(parent, name, fileActions.getFile().getContents(), fileActions.getFile().getContentType());
        } catch (FileAlreadyExistException ex) {
            logger.log(Level.INFO, "Folder create catched: " + ex.getMessage());
            // RequestContext.getCurrentInstance().execute("copyDialog.show();");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Cannot create file with such name!",
                    "Name already exists in folder"));
            logger.log(Level.INFO, "After Folder create catched: " + ex.getMessage());
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
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
        // ====================paging==================treeBean.updatetablePageList();
    }

    public void edit(FSObject selected, TreeNode parent) {
        //TODO rename versionable files

        logger.log(Level.INFO, "selected name: " + selected.getName());
        if (selected instanceof FSFile) {
            UploadedFile file = fileActions.getFile();
            String mimeType = null;
            byte[] content = null;
            if (file != null) {
                content = file.getContents();
                mimeType = file.getContentType();
            }
            try {
                cmisService.edit((FSFile) selected, content, mimeType);

            } catch (FileAlreadyExistException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        "Choose another name!"));
            } catch (UnauthorizedException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        "You must log in for change history"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
        } else { //FSFolder
            try {
                logger.log(Level.INFO, "edit catched: " + selected.getName());
                treeBean.getParent().setPath(cmisService.renameFolder((FSFolder) selected, selected.getName()).getPath());
            } catch (FolderAlreadyExistException ex) {
                logger.log(Level.INFO, "edit catched: " + ex.getMessage());
                logger.log(Level.INFO, "edit catched node: " + ((FSFolder) treeBean.getCachedNode().getData()).getName());

                treeBean.updateTree(treeBean.getCachedNode().getParent());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Folder error!",
                        "Folder with such name already exist!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            if (treeBean.isRowSelected()) {
                treeBean.updateTree(treeBean.getCachedNode());
            } else {
                treeBean.updateTree(treeBean.getCachedNode().getParent());
            }

//            if (parent != null) {
//                logger.log(Level.INFO, "NE NULL SIKA");
//                if(treeBean.getCachedNode().getParent() != null)
//                //    logger.log(Level.INFO, "NE NULL SIKA"+((FSObject)(treeBean.getCachedNode().getParent())).getName());
//                    treeBean.updateTree(treeBean.getCachedNode().getParent());
//            }
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
            treeBean.getParent().setPath(object.getParent().getPath());
            treeBean.updateTree(treeBean.getSelectedNode().getParent());
        } else {
            logger.log(Level.INFO, "deleteSelect++++++" + object.getName());
            cmisService.deleteFile((FSFile) object);
        }
    }

    /**
     * Delete not empty folder with name {@link TreeBean#selectedFSObject}
     * in {@link TreeBean#parent} parent directory
     *
     * @param object folder that is supposed to delete
     */
    public void deleteAllTree(FSObject object) {

        try {
            cmisService.deleteAllTree((FSFolder) object);
        } catch (FolderNotFoundException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "Folder has been deleted already!"));
        }
        treeBean.getParent().setPath("/");
//        treeBean.updateTree(treeBean.getSelectedNode().getParent());
    }


    public void fillHistory(FSFile fsFile) {
        try {
            this.history = cmisService.getHistory(fsFile).getAllVersions();
        } catch (NullPointerException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
    }

    public List<FSFile> getHistory() {
        return history;
    }

    public void setHistory(List<FSFile> history) {
        this.history = history;
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
            if (defaultFolderName.equals("Copy_")) {
                defaultFolderName += folderForCopy.getName();
            } else {
                defaultFolderName =  "Copy_" + folderForCopy.getName();
            }
        }
        return defaultFolderName;
    }

    public void setDefaultFolderName(String defaultFolderName) {
        this.defaultFolderName = defaultFolderName;
    }

    public boolean isCopyItemPressed() {
        return copyItemPressed;
    }

    public void setCopyItemPressed(boolean copyItemPressed) {
        this.copyItemPressed = copyItemPressed;
    }
}
