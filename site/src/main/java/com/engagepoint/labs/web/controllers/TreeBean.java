package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.BrowserRuntimeException;
import com.engagepoint.labs.core.models.exceptions.ConnectionException;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
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
    private TreeNode cachedNode;
    private FSObject selectedFSObject;
    private boolean checkThatSelected;
    private FSFolder parent = new FSFolder();
    private CMISService cmisService;
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private List<PageState> backHistory = new LinkedList<PageState>();
    private List<PageState> forwardHistory = new LinkedList<PageState>();
    private PageState currentPageState;

    private final int firstPage = 1;
    private int currentPage;
    private int lastPage = 1;
    private int amountOfRowsInPage = 5;
    private List<FSObject> tablePageList;
    private String testingCurrentPage;
    private UIComponent messageForPaging;

    private Boolean disableBackButton;
    private Boolean disableNextButton = false;

    private boolean first = true;

    private String folderType;
    private Map<String, String> folderTypes = new HashMap<String, String>();

    public TreeBean() {
        try {
            cmisService = CMISServiceImpl.getService();
        } catch (ConnectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    public void updateTree(TreeNode parent) {
        logger.log(Level.INFO, "UPDATING TREE...."+((FSFolder)parent.getData()).getPath());
        parent.getChildren().clear();
//        logger.log(Level.INFO, "Cast " + (String) parent.getData());
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
                        //                    logger.log(Level.INFO, "FOLDER " + ((FSFolder) treeNode.getData()).getName() + " HAS CHILD FOLDER");
                    }
                    long end = System.currentTimeMillis();
                    //                logger.log(Level.INFO, "TIME: " + (end - start) + "ms");
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
        this.currentPage = pageState.getCurrentPage();
        this.selectedFSObject = pageState.getSelectedObject();
        this.selectedNodes = pageState.getSelectedNode();
        this.parent.setPath(pageState.getParentpath());
        this.selectedNodes.setSelected(true);
        updatetablePageList();
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

    public void setTestingCurrentPage(String testingCurrentPage) {
        if (testingCurrentPage.isEmpty()) {
            this.testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        try {
            Integer.parseInt(testingCurrentPage);
        } catch (NumberFormatException e) {
            this.testingCurrentPage = Integer.toString(currentPage);
            FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage("WRONG PAGE!"));
            return;
        }

        int test = Integer.parseInt(testingCurrentPage);
        try {
            lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "setTestingCurrentPage catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
//            FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WARNING", "somebody delete all pages"));
        } else {
            if (test > lastPage || test < firstPage) {
                if (lastPage != firstPage)
                    FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE!", "it should be between  " + firstPage + " and " + lastPage));
                else
                    FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WARNING", "here is only one page"));
                this.testingCurrentPage = Integer.toString(currentPage);
            } else {
                currentPage = test;
            }
        }
    }

    public void updatetablePageList() {
        try {
            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "updatetablePageList catched: " + e.getMessage());
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
    }

    public void changedTableParentFolder() {
        currentPage = firstPage;
//        tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
        updatetablePageList();
        try {
            lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "changedTableParentFolder catched: " + e.getMessage());

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));

            logger.log(Level.INFO, "changedTableParentFolder catched after: " + e.getMessage());
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        testingCurrentPage = Integer.toString(currentPage);
        disableBackButton = true;
        if (lastPage == 0) {
            lastPage = 1;
            disableNextButton = true;
            return;
        }
        if (currentPage == lastPage) {
            disableNextButton = true;
        } else {
            disableNextButton = false;
        }
    }

    public void nextPage() {
        try {
            lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "nextPage catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        // All this IFs  for dynamic updating number of pages(so all situations are here)
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;

            try {
                tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            } catch (ConnectionException e) {
                logger.log(Level.INFO, "nextPage catched: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        "Connection lost!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage == currentPage) {
            disableNextButton = true;
        }
        if (lastPage < currentPage) {
            if (lastPage > 1) {
                currentPage = lastPage;
            }
            disableNextButton = true;
            if (lastPage == 0 || lastPage == 1) {
                disableBackButton = true;
                currentPage = 1;
            }
            if (lastPage != firstPage) {
                FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE!", "it should be between  " + firstPage + " and " + lastPage));
            } else {
                FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "WARNING", "here is only one page"));
            }
            try {
                tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            } catch (ConnectionException e) {
                logger.log(Level.INFO, "nextPage catched: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        "Connection lost!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (currentPage < lastPage) {
            ++currentPage;
            try {
                tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            } catch (ConnectionException e) {
                logger.log(Level.INFO, "nextPage catched: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        "Connection lost!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            testingCurrentPage = Integer.toString(currentPage);
            if (currentPage == lastPage) {
                disableNextButton = true;
            }
            disableBackButton = false;
        }

    }

    public void previousPage() {
        try {
            lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "previousPage catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }

        // All this IFs  for dynamic updating number of pages(so all situations are here)
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;

            try {
                tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            } catch (ConnectionException e) {
                logger.log(Level.INFO, "previousPage catched: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        "Connection lost!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage < currentPage && lastPage > 1) {
            currentPage = lastPage;
            disableNextButton = true;
            // FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "UPDATING", "Somebody delete folders in this node"));

            try {
                tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            } catch (ConnectionException e) {
                logger.log(Level.INFO, "previousPage catched: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        "Connection lost!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage == 0 || lastPage == 1) {
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;
            // FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "UPDATING", "Somebody delete folders in this node"));

            try {
                tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            } catch (ConnectionException e) {
                logger.log(Level.INFO, "previousPage catched: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        "Connection lost!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            testingCurrentPage = Integer.toString(currentPage);
        }
        if (lastPage == currentPage) {
            disableNextButton = true;
        }
        if (currentPage > firstPage) {
            --currentPage;

            try {
                tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            } catch (ConnectionException e) {
                logger.log(Level.INFO, "previousPage catched: " + e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        "Connection lost!"));
            } catch (BaseException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        e.getMessage(),
                        ""));
            }
            testingCurrentPage = Integer.toString(currentPage);

            if (currentPage == firstPage) {
                disableBackButton = true;
            }
            disableNextButton = false;
        }
    }

    public void currentPageToJSF() {
        if (currentPage > lastPage || currentPage < firstPage) {
            if (lastPage != firstPage) {
                FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE!", "it should be between  " + firstPage + " and " + lastPage));
            } else {
                FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WARNING", "here is only one page"));
            }
        }
        if (currentPage == firstPage) {
            disableBackButton = true;
        } else {
            disableBackButton = false;
        }
        if (currentPage == lastPage) {
            disableNextButton = true;
        } else {
            disableNextButton = false;
        }

        try {
            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "currentPageToJSF catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        testingCurrentPage = Integer.toString(currentPage);
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
            state.setCurrentPage(currentPage);
            state.setSelectedNode(selectedNodes);
            state.setSelectedObject(selectedFSObject);
            state.setParentpath(parent.getPath());
            addToBack(state);
            this.cachedNode = selectedNodes;
            this.selectedNodes.setSelected(false);
        }
    }

    public void onRowSelect(SelectEvent event) {
        if (selectedNodes != null) {
            this.cachedNode = selectedNodes;
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
        TreeNode tempNode =  event.getDragNode().getParent();
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
                fileActions.setVersionable(((FSFile) sn).isVersionable());
            } else {
                fileActions.setSelectedIsFile(false);
            }
            fileActions.setSelectedName(sn.getName());
            this.selectedFSObject = sn;
            logger.log(Level.INFO, "id: " + sn.getId());
        }
    }

    public FSFolder getParent() {
        return parent;
    }

    public void setParent(FSFolder parent) {
        this.parent = parent;
    }

    public Boolean getDisableNextButton() {
        return disableNextButton;
    }

    public Boolean getDisableBackButton() {
        return disableBackButton;
    }

    public String getTestingCurrentPage() {
        return testingCurrentPage;
    }

    public List<FSObject> getTablePageList() {
        return tablePageList;
    }

    public void setTablePageList(List<FSObject> tablePageList) {
        this.tablePageList = tablePageList;
    }

    public int getAmountOfRowsInPage() {
        return amountOfRowsInPage;
    }

    public void setAmountOfRowsInPage(int amountOfRowsInPage) {
        this.amountOfRowsInPage = amountOfRowsInPage;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public UIComponent getMessageForPaging() {
        return messageForPaging;
    }

    public void setMessageForPaging(UIComponent messageForPaging) {
        this.messageForPaging = messageForPaging;
    }

    public void setDisableBackButton(Boolean disableBackButton) {
        this.disableBackButton = disableBackButton;
    }

    public void setDisableNextButton(Boolean disableNextButton) {
        this.disableNextButton = disableNextButton;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public FileActions getFileActions() {
        return fileActions;
    }

    public void setFileActions(FileActions fileActions) {
        this.fileActions = fileActions;
    }

    public TreeNode getCachedNode() {
        return cachedNode;
    }

    public void setCachedNode(TreeNode cachedNode) {
        logger.log(Level.INFO, "SetCachedNode");
        if (cachedNode != null)
            this.cachedNode = cachedNode;
    }
}