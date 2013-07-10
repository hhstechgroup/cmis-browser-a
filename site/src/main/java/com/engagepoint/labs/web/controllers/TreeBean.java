package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.*;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
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
    private FSFolder parent = new FSFolder();
    private CMISService cmisService = CMISServiceImpl.getService();
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private List<PageState> backHistory = new LinkedList<PageState>();
    private List<PageState> forwardHistory = new LinkedList<PageState>();
    private PageState currentPageState;

    private final int firstPage = 1;
    private int currentPage;
    private int lastPage = 100;
    private int amountOfRowsInPage = 5;
    private List<FSObject> tablePageList;
    private String testingCurrentPage;
    private UIComponent messageForPaging;

    private Boolean disableBackButton;
    private Boolean disableNextButton = false;

    public TreeBean() {
        FSFolder root = cmisService.getRootFolder();
        parent.setPath("/");
        main = new DefaultTreeNode("Main", null);
        TreeNode node0 = new DefaultTreeNode(root, main);
        FSFolder fold = new FSFolder();
        fold.setName("Empty Folder");
        new DefaultTreeNode(fold, node0);

        changedTableParentFolder();
    }

    public void updateTree(TreeNode parent) {
        logger.log(Level.INFO, "UPDATING... TREEEE...");
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
                    logger.log(Level.INFO, "FOLDER " + ((FSFolder) treeNode.getData()).getName() + " HAS CHILD FOLDER");
                }
                long end = System.currentTimeMillis();
                logger.log(Level.INFO, "TIME: " + (end - start) + "ms");
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
        backHistory.remove(0);
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
        changedTableParentFolder();
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
        lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);
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

    public void changedTableParentFolder() {
        currentPage = firstPage;
        tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
        lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);
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
        lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        // All this IFs  for dynamic updating number of pages(so all situations are here)
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;

            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
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
            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (currentPage < lastPage) {
            ++currentPage;
            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            if (currentPage == lastPage) {
                disableNextButton = true;
            }
            disableBackButton = false;
        }

    }

    public void previousPage() {
        lastPage = cmisService.getMaxNumberOfPage(parent, amountOfRowsInPage);

        // All this IFs  for dynamic updating number of pages(so all situations are here)
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;

            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage < currentPage && lastPage > 1) {
            currentPage = lastPage;
            disableNextButton = true;
            // FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "UPDATING", "Somebody delete folders in this node"));

            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage == 0 || lastPage == 1) {
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;
            // FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "UPDATING", "Somebody delete folders in this node"));

            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
        }
        if (lastPage == currentPage) {
            disableNextButton = true;
        }
        if (currentPage > firstPage) {
            --currentPage;

            tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
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

        tablePageList = cmisService.getPage(parent, currentPage, amountOfRowsInPage);
        testingCurrentPage = Integer.toString(currentPage);
    }

    public void setSelectedNode(TreeNode selectedNodes) {
        logger.log(Level.INFO, "setSelectedNode");
        if(selectedNodes != null) {
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
            this.selectedNodes.setSelected(false);
        }
    }

    public void onRowSelect(SelectEvent event) {
        if(selectedNodes != null) {
            this.selectedNodes.setSelected(false);
        }
        setSelectedFSObject((FSObject) event.getObject());
    }

    public void onDragDrop(TreeDragDropEvent event) {
        TreeNode dragNode = event.getDragNode();
        TreeNode dropNode = event.getDropNode();
        int dropIndex = event.getDropIndex();
        FSFolder fff = (FSFolder)dragNode.getData();
        FSFolder ddd = (FSFolder)dropNode.getData();

        logger.log(Level.INFO, "DragNode data = "+fff.getPath());
        logger.log(Level.INFO, "DropNode data = "+ddd.getPath());
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Dragged " + dragNode.getData(), "Dropped on " + dropNode.getData() + " at " + dropIndex);
        FacesContext.getCurrentInstance().addMessage(null, message);
        cmisService.move(fff,ddd);
        logger.log(Level.INFO, "MOVED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

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
            if(sn instanceof FSFile) {
                fileActions.setSelectedIsFile(true);
            } else {
                fileActions.setSelectedIsFile(false);
            }
            fileActions.setSelectedName(sn.getName());
            this.selectedFSObject = sn;
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
}