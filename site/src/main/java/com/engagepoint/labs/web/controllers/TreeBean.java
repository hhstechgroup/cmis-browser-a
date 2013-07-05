package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
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

    private TreeNode main;
    private TreeNode selectedNodes;
    private FSObject selectedFSObject;
    private FSFolder parent = new FSFolder();
    private CMISService CMISService = CMISServiceImpl.getService();
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private List<PageState> backHistory = new LinkedList<PageState>();
    private List<PageState> forwardHistory = new LinkedList<PageState>();
    private PageState currentPageState;

    private final int firstPage = 1;
    private int currentPage;
    private int lastPage = 100;
    private int amountOfRowsInPage = 1;
    private List<FSObject> tablePageList;
    private String testingCurrentPage;
    private UIComponent messageForPaging;

    private Boolean disableBackButton;
    private Boolean disableNextButton = false;

    public TreeBean() {
        updateTree();
    }

    public void updateTree() {
        logger.log(Level.INFO, "UPDATING... TREEEE...");
        FSFolder root = CMISService.getRootFolder();
        parent.setPath("/");
        main = new DefaultTreeNode("Main", null);
        TreeNode node0 = new DefaultTreeNode(root, main);
        SubObjects(parent, node0);
        //for paging
        changedTableParentFolder();
    }

    public void doBack() {
        if (backHistory.size() == 0) return;
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
        if (forwardHistory.size() == 0) return;
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

    private void SubObjects(FSFolder parent, TreeNode treenodeparent) {
        List<FSObject> children = CMISService.getChildren(parent);
        for (FSObject i : children) {
            if (i instanceof FSFolder) {
                TreeNode treeNode = new DefaultTreeNode(i, treenodeparent);
                SubObjects((FSFolder) i, treeNode);
            }
        }
    }

    public void setTestingCurrentPage(String testingCurrentPage) {
        if (testingCurrentPage == "") {
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
        lastPage = CMISService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
//            FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WARNING", "somebody delete all pages"));
        } else {
            if (test > lastPage || test < firstPage) {
                FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE!", "here should be number between " + firstPage + " and " + lastPage));
                this.testingCurrentPage = Integer.toString(currentPage);
            } else {
                currentPage = test;
            }
        }
    }


    public void changedTableParentFolder() {
        currentPage = firstPage;
        tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
        lastPage = CMISService.getMaxNumberOfPage(parent, amountOfRowsInPage);
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
        lastPage = CMISService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        // All this IFs  for dynamic updating number of pages(so all situations are here)
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;

            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage == currentPage) {
            disableNextButton = true;
        }
        if (lastPage < currentPage) {
            if (lastPage > 1) currentPage = lastPage;
            disableNextButton = true;
            if (lastPage == 0 || lastPage == 1) {
                disableBackButton = true;
                currentPage = 1;
            }
            FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE!", "it should be between  " + firstPage + " and " + lastPage));
            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (currentPage < lastPage) {
            ++currentPage;
            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            if (currentPage == lastPage) disableNextButton = true;
            disableBackButton = false;
        }

    }

    public void previousPage() {
        lastPage = CMISService.getMaxNumberOfPage(parent, amountOfRowsInPage);

        // All this IFs  for dynamic updating number of pages(so all situations are here)
        if (lastPage == 0) {
            lastPage = 1;
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;

            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage < currentPage && lastPage > 1) {
            currentPage = lastPage;
            disableNextButton = true;
           // FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "UPDATING", "Somebody delete folders in this node"));

            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            return;
        }
        if (lastPage == 0 || lastPage == 1) {
            currentPage = 1;
            disableNextButton = true;
            disableBackButton = true;
           // FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "UPDATING", "Somebody delete folders in this node"));

            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
        }
        if (lastPage == currentPage) {
            disableNextButton = true;
        }
        if (currentPage > firstPage) {
            --currentPage;

            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);

            if (currentPage == firstPage) disableBackButton = true;
            disableNextButton = false;
        }
    }

    public void CurrentPageToJSF() {
        if (currentPage > lastPage || currentPage < firstPage)
            FacesContext.getCurrentInstance().addMessage(messageForPaging.getClientId(), new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE!", "it should be between  " + firstPage + " and " + lastPage));
        if (currentPage == firstPage) disableBackButton = true;
        else disableBackButton = false;
        if (currentPage == lastPage) disableNextButton = true;
        else disableNextButton = false;

        tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
        testingCurrentPage = Integer.toString(currentPage);
    }

    public void setSelectedNode(TreeNode selectedNodes) {
        logger.log(Level.INFO, "setSelectedNode");
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

    public void onRowSelect(SelectEvent event) {
        this.selectedNodes.setSelected(false);
        this.selectedFSObject = (FSObject) event.getObject();
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
        if (sn != null)
            this.selectedFSObject = sn;
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
}