package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
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
@ApplicationScoped
public class TreeBean implements Serializable {

    private boolean backButtonDisabled = true;
    private boolean forwardButtonDisabled = true;
    private TreeNode main;
    private TreeNode selectedNodes;
    private FSObject selectedFSObject;
    private FSFolder parent = new FSFolder();
    private CMISService CMISService = CMISServiceImpl.getService();
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());
    private List<FSObject> navigationList;

    private static int number = 2;

    private int backCounter = 0;

    private final int firstPage = 1;
    private int currentPage;
    private int lastPage = 100;
    private int amountOfRowsInPage = 3;
    private List<FSObject> tablePageList;
    private String testingCurrentPage;

    private Boolean disableBackButton;
    private Boolean disableNextButton = false;

    public TreeBean() {
        navigationList = new LinkedList<FSObject>();

        FSFolder root = CMISService.getRootFolder();
        parent.setPath("/");
        main = new DefaultTreeNode("Main", null);
        TreeNode node0 = new DefaultTreeNode(root, main);

        FSFolder fold = new FSFolder();
        fold.setName("Empty Folder");
        new DefaultTreeNode(fold, node0);

        //for paging
        changedTableParentFolder();
    }

    public void updateTree(TreeNode parent) {
        logger.log(Level.INFO, "UPDATING... TREEEE...");
        parent.getChildren().clear();
        List<FSObject> children = CMISService.getChildren((FSFolder)parent.getData());
        for (FSObject i : children) {
            if (i instanceof FSFolder) {
                TreeNode treeNode = new DefaultTreeNode(i, parent);
                if (CMISService.hasChildFolder((FSFolder) treeNode.getData())) {
                    FSFolder fold = new FSFolder();
                    fold.setName("Empty Folder");
                    new DefaultTreeNode(fold, treeNode);
                    logger.log(Level.INFO, "FOLDER "+((FSFolder) treeNode.getData()).getName()+" HAS CHILD FOLDER");
                }
            }
        }
    }
    public void onNodeExpand(NodeExpandEvent event) {
        updateTree(event.getTreeNode()) ;
    }

    /**
     * if files in our repository has changed when node collapse
     * we clear all children and check if node has children
     * @param event   that is fired on NodeCollapse
     */
    public void onNodeCollapse( NodeCollapseEvent event) {
        event.getTreeNode().getChildren().clear();
        if (CMISService.hasChildFolder((FSFolder) event.getTreeNode().getData())) {
            FSFolder fold = new FSFolder();
            fold.setName("Empty Folder");
            new DefaultTreeNode(fold, event.getTreeNode());
        }
    }

    public void addNewNode(ActionEvent event) {
        FSFolder fold = new FSFolder();
        fold.setName("Empty Folder");
        new DefaultTreeNode(fold, selectedNodes);
    }


    public void setTestingCurrentPage(String testingCurrentPage) {
        this.testingCurrentPage = testingCurrentPage;
        if (testingCurrentPage == "") return;
        try {
            Integer.parseInt(testingCurrentPage);
        } catch (NumberFormatException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE DUDE!!!", "here should be number between" + firstPage + " and " + lastPage));
            return;
        }
        int test = Integer.parseInt(testingCurrentPage);
        if (test > lastPage || test < firstPage)
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE DUDE!!!", "here should be number between" + firstPage + " and trololoool " + lastPage));
        else {
            currentPage = test;

            if (currentPage == lastPage) disableNextButton = true;
            else disableNextButton = false;

            if (currentPage == firstPage) disableBackButton = true;
            else disableBackButton = false;

            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
        }
    }

    public void changedTableParentFolder() {
        currentPage = firstPage;
        tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
        lastPage = CMISService.getMaxNumberOfPage(parent, amountOfRowsInPage);
        testingCurrentPage = Integer.toString(currentPage);
        disableBackButton = true;
        if (lastPage == 0) lastPage = 1;
        if (currentPage == lastPage) {
            disableNextButton = true;
        } else {
            disableNextButton = false;
        }
    }

    public void nextPage() {
        if (currentPage < lastPage) {
            ++currentPage;
            tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
            testingCurrentPage = Integer.toString(currentPage);
            if (currentPage == lastPage) disableNextButton = true;
            disableBackButton = false;
        }

    }

    public void previousPage() {
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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "WRONG PAGE DUDE!!!", "it should be between " + firstPage + " and " + lastPage));
        if (currentPage == firstPage) disableBackButton = true;
        if (currentPage == lastPage) disableNextButton = true;
        tablePageList = CMISService.getPage(parent, currentPage, amountOfRowsInPage);
    }

    /**
     * Sets the selected node in the tree.
     *
     * @param selectedNodes node to be set as selected.
     */
    public void setSelectedNode(TreeNode selectedNodes) {

        this.selectedNodes = selectedNodes;

        FSObject tmp = (FSObject) selectedNodes.getData();
        logger.log(Level.INFO, "setSelectedNode  tmp null? - " + (tmp == null));
        setSelectedFSObject(tmp);

        int index = navigationList.size() - backCounter;

        logger.log(Level.INFO, "INDEX: " + index);
        int temp = 0;
        for (; index < navigationList.size() + temp; ++index) {
            int s = temp++;
            logger.log(Level.INFO, "object: " + navigationList.get(index - s).getName() + " REMOVED");
            setNumber(getNumber() - 1);
            navigationList.remove(index - s);
        }
        //TODO HARDCORE LIST[0] = ROOT
        if (navigationList.size() == 0) {
            navigationList.add(selectedFSObject);
        }
        if ((navigationList.size() > 0) && (!selectedFSObject.equals(navigationList.get(navigationList.size() - 1)))) {
            navigationList.add(selectedFSObject);
        } else {
            logger.log(Level.INFO, "Item exist");
        }
        setForwardButtonDisabled(true);
        if (navigationList.size() > 1 && isBackButtonDisabled()) {
            logger.log(Level.INFO, "VKL BACK BUT");
            setBackButtonDisabled(false);
        }
        this.backCounter = 0;
        for (int i = 0; i < navigationList.size(); i++) {
            logger.log(Level.INFO, navigationList.get(i).getName());
        }

        if (selectedFSObject.getPath() == null) {
            parent.setPath("/");
            selectedFSObject.setPath("/");
        } else {
            parent.setPath(selectedFSObject.getPath());
        }

        logger.log(Level.INFO, "_NUMBER: " + getNumber());

        //for paging
        changedTableParentFolder();
    }

    public void backButton() {
        logger.log(Level.INFO, "SECOND");
        backCounter++;
        logger.log(Level.INFO, "SIZE: " + navigationList.size());
        logger.log(Level.INFO, "NUMBER: " + getNumber());
        FSObject currentObject = navigationList.get(navigationList.size() - getNumber());
        setNumber(getNumber() + 1);
        if ((navigationList.size() - getNumber()) < 0) {
            setBackButtonDisabled(true);
        }
        setForwardButtonDisabled(false);
        if (selectedFSObject.getPath() == null) {
            parent.setPath("/");
            selectedFSObject.setPath("/");
        } else {
            parent.setPath(currentObject.getPath());
        }
        changedTableParentFolder();
        logger.log(Level.INFO, "__NUMBER: " + getNumber());
    }

    public void forwardButton() {
        backCounter--;
        logger.log(Level.INFO, "SIZE: " + navigationList.size());
        logger.log(Level.INFO, "NUMBER: " + getNumber());
        FSObject currentObject = navigationList.get(navigationList.size() - getNumber() + 2);
        setNumber(getNumber() - 1);
        if (getNumber() <= 2) {
            setForwardButtonDisabled(true);
        }
        setBackButtonDisabled(false);

        System.out.println("FORWARD BUTTON________________________________________" + currentObject.getName());

        if (selectedFSObject.getPath() == null) {
            parent.setPath("/");
            selectedFSObject.setPath("/");
        } else {
            parent.setPath(currentObject.getPath());
        }
        changedTableParentFolder();
        logger.log(Level.INFO, "NUMBER__: " + getNumber());
    }

    public void onRowSelect(SelectEvent event) {
        if (event == null) {
            logger.log(Level.INFO, "EVENT NULL");
        }
        this.selectedFSObject = (FSObject) event.getObject();
        logger.log(Level.INFO, "onRowSelect: " + selectedFSObject.getName());
    }

    public static int getNumber() {
        return number;
    }

    public static void setNumber(int number) {
        TreeBean.number = number;
    }

    public TreeNode getModel() {
//        return root;
        return main;
    }

    /**
     * Gets the selected node in the tree.
     *
     * @return selected node in tree.
     */

    public TreeNode getSelectedNode() {
        return selectedNodes;
    }

    public FSObject getSelectedFSObject() {
        return selectedFSObject;
    }

    public void setSelectedFSObject(FSObject sn) {
        logger.log(Level.INFO, "setSelectedFSObject sn - null? - " + (sn == null));
        if (sn != null)
            this.selectedFSObject = sn;
    }

    public FSFolder getParent() {
        return parent;
    }

    public void setParent(FSFolder parent) {
        this.parent = parent;
    }

    public boolean isForwardButtonDisabled() {
        return forwardButtonDisabled;
    }

    public void setForwardButtonDisabled(boolean forwardButtonDisabled) {
        this.forwardButtonDisabled = forwardButtonDisabled;
    }

    public boolean isBackButtonDisabled() {
        return backButtonDisabled;
    }

    public void setBackButtonDisabled(boolean backButtonDisabled) {
        this.backButtonDisabled = backButtonDisabled;
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
    //fail method
    private void SubObjects(FSFolder parent, TreeNode treenodeparent) {
        List<FSObject> children = CMISService.getChildren(parent);
        for (FSObject i : children) {
            if (i instanceof FSFolder) {
                TreeNode treeNode = new DefaultTreeNode(i, treenodeparent);
                SubObjects((FSFolder) i, treeNode);
            }
        }
    }
}