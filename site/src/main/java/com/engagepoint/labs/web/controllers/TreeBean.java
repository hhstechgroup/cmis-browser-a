package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.BrowserRuntimeException;
import com.engagepoint.labs.core.models.exceptions.ConnectionException;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import com.engagepoint.labs.web.models.LazyFSObjectDataModel;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private CMISService cmisService;
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private List<PageState> backHistory = new LinkedList<PageState>();
    private List<PageState> forwardHistory = new LinkedList<PageState>();
    private PageState currentPageState;

    private LazyFSObjectDataModel lazyModel;

    private boolean first = true;

    private String folderType;
    private Map<String, String> folderTypes = new HashMap<String, String>();


    private String findQuery;


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
        try {
            cmisService = CMISServiceImpl.getService();
        } catch (ConnectionException e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
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
        logger.log(Level.INFO, "UPDATING TREE...."+((FSFolder)parent.getData()).getPath());
        parent.getChildren().clear();
// logger.log(Level.INFO, "Cast " + (String) parent.getData());
        try {
            List<FSObject> children = cmisService.getChildren((FSFolder) parent.getData());

            for (FSObject child : children) {
                if (child instanceof FSFolder) {
                    TreeNode treeNode = new DefaultTreeNode(child, parent);
                    long start = System.currentTimeMillis();
                    if (cmisService.hasChildFolder((FSFolder) treeNode.getData())) {
                        FSFolder fold = new FSFolder();
                        fold.setName("Empty Folder");
                        new DefaultTreeNode(fold, treeNode);
                        // logger.log(Level.INFO, "FOLDER " + ((FSFolder) treeNode.getData()).getName() + " HAS CHILD FOLDER");
                    }
                    long end = System.currentTimeMillis();
                    // logger.log(Level.INFO, "TIME: " + (end - start) + "ms");
                }
            }
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "updateTree catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
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

    public void onNodeExpand(NodeExpandEvent event) throws BaseException {
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
        try {
            if (cmisService.hasChildFolder((FSFolder) event.getTreeNode().getData())) {
                FSFolder fold = new FSFolder();
                fold.setName("Empty Folder");
                new DefaultTreeNode(fold, event.getTreeNode());
            }
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "onNodeCollapse catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
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
            if (findQuery.isEmpty()) {
                changedTableParentFolder();
            }
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

    public void onDragDrop(TreeDragDropEvent event) {
        TreeNode dragNode = event.getDragNode();
        TreeNode dropNode = event.getDropNode();

        TreeNode parentDragNode = dragNode.getParent();
        logger.log(Level.INFO, "Rostik1: "+ ((FSFolder)parentDragNode.getData()).getPath());
        FSFolder dragedFolder = (FSFolder) dragNode.getData();
        FSFolder dropedFolder = (FSFolder) dropNode.getData();
        TreeNode tempNode = event.getDragNode().getParent();
        FSFolder dropedFolder1 = (FSFolder) dropNode.getData();
        logger.log(Level.INFO, "Before DragNode data = " + dragedFolder.getPath());
        logger.log(Level.INFO, "Before DropNode data = " + dropedFolder.getPath());
        logger.log(Level.INFO, "Before DropNode data = " + dropedFolder1.getPath());

        try {
            cmisService.move(dragedFolder, dropedFolder);
        } catch (BrowserRuntimeException e) {
            FSFolder drag = (FSFolder) dragNode.getData();
            FSFolder drop = (FSFolder) dragNode.getData();
            FSFolder temp = (FSFolder) tempNode.getData();
            TreeNode d = event.getDragNode();
            logger.log(Level.INFO, "After dragedFolder data = " + ((FSFolder)(d.getParent().getData())).getPath());
            logger.log(Level.INFO, "After dragedFolder data = " + ((FSFolder)(d.getData())).getPath());

            logger.log(Level.INFO, "After dragedFolder data = " + dragedFolder.getParent().getPath());
            logger.log(Level.INFO, "After dragedFolder data = " + dropedFolder.getPath());
            logger.log(Level.INFO, "After dragedFolder1 data = " + dropedFolder1.getPath());

            logger.log(Level.INFO, "After dragedFolder1 data = " + drag.getParent().getPath());
            logger.log(Level.INFO, "After DropNode data = " + drop.getParent().getPath());
            logger.log(Level.INFO, "After dragedFolder1 data = " + temp.getParent().getPath());

            logger.log(Level.INFO, "After DropNode data = " + ((FSFolder) dragNode.getData()).getParent().getName());
            logger.log(Level.INFO, "After DropNode data = " + ((FSFolder) dropNode.getData()).getParent().getName());
            logger.log(Level.INFO, "After DropNode data = " + ((FSFolder) tempNode.getData()).getParent().getPath());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "This name already exists in folder!"));
            updateTree(parentDragNode);
            logger.log(Level.INFO, "Rostik2: "+ ((FSFolder)parentDragNode.getData()).getPath());

        } finally {
            logger.log(Level.INFO, "MOVED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");


        }

    }

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

    public LazyFSObjectDataModel getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyFSObjectDataModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public String getFindQuery() {
        logger.log(Level.INFO, "get=" + findQuery);
        return findQuery;
    }

    public void setFindQuery(String findQuery) {
        if (findQuery == null) {
            this.findQuery = "";
            getLazyModel().setSearchQuery(findQuery);
            logger.log(Level.INFO, "setFIndQuery=nulllll" + findQuery);
        } else {
            this.findQuery = findQuery;
            getLazyModel().setSearchQuery(findQuery);
            logger.log(Level.INFO, "setFindQuery=-------------- " + getLazyModel().getSearchQuery());

        }
    }
}