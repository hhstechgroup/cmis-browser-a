package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import com.engagepoint.labs.web.models.LazyFSObjectDataModel;
import org.primefaces.context.RequestContext;
import org.primefaces.event.*;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: bogdan.ezapenkin
 * Date: 6/17/13
 * Time: 4:37 PM
 */

@ManagedBean(name = "treeBean")
@SessionScoped
public class TreeBean implements Serializable {

    @ManagedProperty(value = "#{fileActions}")
    private FileActions fileActions;

    private TreeNode main;
    private TreeNode selectedNodes;
    private FSObject selectedFSObject;
    private boolean checkThatSelected;
    private FSFolder parent = new FSFolder();
    private CMISService cmisService = CMISServiceImpl.getService();
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private List<PageState> backHistory = new LinkedList<PageState>();
    private List<PageState> forwardHistory = new LinkedList<PageState>();
    private PageState currentPageState;

    private LazyDataModel<FSObject> lazyModel;

    private boolean first = true;

    private String folderType;
    private Map<String, String> folderTypes = new HashMap<String, String>();

    public String getFolderType() {
        return folderType;
    }

    public void setFolderType(String folderType) {
        this.folderType = folderType;
    }


    public Map<String, String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(Map<String, String> folderTypes) {
        this.folderTypes = folderTypes;
    }


    public TreeBean() {
        FSFolder root = cmisService.getRootFolder();
        parent.setPath("/");
        main = new DefaultTreeNode("Main", null);
        TreeNode node0 = new DefaultTreeNode(root, main);
        FSFolder fold = new FSFolder();
        fold.setName("Empty Folder");
        new DefaultTreeNode(fold, node0);
        this.selectedNodes = node0;
        changedTableParentFolder();
        folderTypes.put("CMIS Folder (cmis:folder)", "CMIS Folder (cmis:folder)");
    }

    public void updateTree(TreeNode parent) {
//        logger.log(Level.INFO, "UPDATING... TREEEE...");
        parent.getChildren().clear();

        List<FSObject> children = cmisService.getChildren((FSFolder) parent.getData());

        for (FSObject child : children) {
            if (child instanceof FSFolder) {
                TreeNode treeNode = new DefaultTreeNode(child, parent);
                long start = System.currentTimeMillis();
                if (cmisService.hasChildFolder((FSFolder) treeNode.getData())) {
                    FSFolder fold = new FSFolder();
                    fold.setName("Empty Folder");
                    new DefaultTreeNode(fold, treeNode);
//                    logger.log(Level.INFO, "FOLDER " + ((FSFolder) treeNode.getData()).getName() + " HAS CHILD FOLDER");
                }
                long end = System.currentTimeMillis();
//                logger.log(Level.INFO, "TIME: " + (end - start) + "ms");
            }
        }
    }

    public void doBack() {
        logger.log(Level.INFO, "doBack");
        if (backHistory.size() == 0) {
            return;
        }
        for (int i = 0; i < backHistory.size(); i++) {
            logger.log(Level.INFO, "BEFORE doBack REMOVE name: " + backHistory.get(i).getSelectedObject().getName());
        }
        if (!first) {
            backHistory.remove(0);
        }
        first = false;
        currentPageState = backHistory.remove(0);
        addToForward(currentPageState);
        currentPageState = backHistory.get(0);
        for (int i = 0; i < backHistory.size(); i++) {
            logger.log(Level.INFO, "AFTER doBack REMOVE name: " + backHistory.get(i).getSelectedObject().getName());
        }
        updateBean(currentPageState);
    }

    public void doForward() {
        if (forwardHistory.size() == 0) {
            return;
        }
        for (int i = 0; i < forwardHistory.size(); i++) {
            logger.log(Level.INFO, "BEFORE REMOVE name: " + forwardHistory.get(i).getSelectedObject().getName());
        }
        currentPageState = forwardHistory.remove(0);
        backHistory.remove(0);
        logger.log(Level.INFO, "REMOVE name: " + currentPageState.getSelectedObject().getName());
        for (int i = 0; i < forwardHistory.size(); i++) {
            logger.log(Level.INFO, "AFTER REMOVE name: " + forwardHistory.get(i).getSelectedObject().getName());
        }
        updateBean(currentPageState);
        addToBack(currentPageState);
    }

    public void addToBack(PageState state) {
        logger.log(Level.INFO, "addToBack name: " + state.getSelectedObject().getName());
        backHistory.add(0, state);
    }

    public void addToForward(PageState state) {
        logger.log(Level.INFO, "addToForward name: " + state.getSelectedObject().getName());
        forwardHistory.add(0, state);
    }

    public void updateBean(PageState pageState) {
        logger.log(Level.INFO, "updateBean");
//        this.currentPage = pageState.getCurrentPage();
        this.selectedFSObject = pageState.getSelectedObject();
        this.selectedNodes = pageState.getSelectedNode();
        this.parent.setPath(pageState.getParentpath());
        this.selectedNodes.setSelected(true);
//        updatetablePageList();
    }

    public void onNodeExpand(NodeExpandEvent event) {
        updateTree(event.getTreeNode());
    }

    /**
     * if files in our repository has changed when node collapse
     * we clear all children and check if node has children
     *
     * @param event that is fired on NodeCollapse
     */
    public void onNodeCollapse(NodeCollapseEvent event) {
        logger.log(Level.INFO, "onNodeCollapse");
        event.getTreeNode().getChildren().clear();
        if (cmisService.hasChildFolder((FSFolder) event.getTreeNode().getData())) {
            FSFolder fold = new FSFolder();
            fold.setName("Empty Folder");
            new DefaultTreeNode(fold, event.getTreeNode());
        }
    }




    public void changedTableParentFolder() {
        lazyModel = new LazyFSObjectDataModel(cmisService, parent);
    }



    public void setSelectedNode(TreeNode selectedNodes) {
        logger.log(Level.INFO, "setSelectedNode");
        if (selectedNodes != null) {
            this.selectedNodes = selectedNodes;
            setSelectedFSObject((FSObject) selectedNodes.getData());
            if (selectedFSObject.getPath() == null) {
                parent.setPath("/");
                selectedFSObject.setPath("/");
            } else {
                parent.setPath(selectedFSObject.getPath());
            }
            changedTableParentFolder();
            PageState state = new PageState();
//            state.setCurrentPage(currentPage);
            state.setSelectedNode(selectedNodes);
            state.setSelectedObject(selectedFSObject);
            state.setParentpath(parent.getPath());
            addToBack(state);
            this.selectedNodes.setSelected(false);
        }
    }

    public void onRowSelect(SelectEvent event) {
        if (selectedNodes != null) {
            this.selectedNodes.setSelected(false);
        }
        setSelectedFSObject((FSObject) event.getObject());
    }

//    public void onDragDrop(TreeDragDropEvent event) {
//        TreeNode dragNode = event.getDragNode();
//        TreeNode dropNode = event.getDropNode();
//        int dropIndex = event.getDropIndex();
//        FSFolder fff = (FSFolder)dragNode.getData();
//        FSFolder ddd = (FSFolder)dropNode.getData();
//
//        logger.log(Level.INFO, "DragNode data = "+fff.getPath());
//        logger.log(Level.INFO, "DropNode data = "+ddd.getPath());
//        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Dragged " + dragNode.getData(), "Dropped on " + dropNode.getData() + " at " + dropIndex);
//        FacesContext.getCurrentInstance().addMessage(null, message);
//        cmisService.move(fff,ddd);
//        logger.log(Level.INFO, "MOVED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        updateTree(dragNode);
//        updateTree(dropNode);
//    }

    public boolean isCheckThatSelected() {
        return selectedFSObject != null ? true : false;
    }

    public TreeNode getRoot() {
        return main;
    }

    public TreeNode getSelectedNode() {
        return selectedNodes;
    }

    public FSObject getSelectedFSObject() {
        return selectedFSObject;
    }

    public void setSelectedFSObject(FSObject sn) {
        if (sn != null) {
            if (sn instanceof FSFile) {
                fileActions.setSelectedIsFile(true);
            } else {
                fileActions.setSelectedIsFile(false);
            }
            fileActions.setSelectedName(sn.getName());
            this.selectedFSObject = sn;
            logger.log(Level.INFO, "selectedObject: " + sn.getName());
        }
    }

    public FSFolder getParent() {
        return parent;
    }

    public void setParent(FSFolder parent) {
        this.parent = parent;
    }

    public FileActions getFileActions() {
        return fileActions;
    }

    public void setFileActions(FileActions fileActions) {
        this.fileActions = fileActions;
    }

    public LazyDataModel<FSObject> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyDataModel<FSObject> lazyModel) {
        this.lazyModel = lazyModel;
    }
}