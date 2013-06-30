package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
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
    private List<FSObject> fsList;
    private List<FSObject> navigationList;

    private static int number = 2;


    public TreeBean() {
        navigationList = new LinkedList<FSObject>();
        updateTree();
    }

    public void updateTree() {
        logger.log(Level.INFO, "UPDATING... TREEEE...");
        FSFolder root = CMISService.getRootFolder();
        parent = root;
        fsList = CMISService.getChildren(parent);
        main = new DefaultTreeNode("Main", null);
        TreeNode node0 = new DefaultTreeNode(root, main);
        SubObjects(parent, node0);
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

    public void setSelectedNode(TreeNode selectedNodes) {
        if (navigationList.size() == 1 && isBackButtonDisabled()) {
            setBackButtonDisabled(false);
        }
        this.selectedNodes = selectedNodes;
        FSObject tmp = (FSObject) selectedNodes.getData();
        if(tmp != null)
            this.selectedFSObject = tmp;
        if (selectedFSObject.getPath() == null) {
            parent.setPath("/");
            selectedFSObject.setPath("/");
            navigationList.add(selectedFSObject);
        } else {
            parent.setPath(selectedFSObject.getPath());
        }
        fsList = CMISService.getChildren(parent);
    }

    public void backButton() {
        navigationList.remove(navigationList.size()-1);
        System.out.println(navigationList.size() + " size111111111111111111111111111111111111111111111111");
        FSObject currentObject = navigationList.get(navigationList.size() - getNumber());
        System.out.println(navigationList.size() + " size222222222222222222222222222222222222222222222222");
        System.out.println(currentObject.getPath() + " baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        setNumber(getNumber() + 1);
        if ((navigationList.size() - getNumber()) < 0) {
            setBackButtonDisabled(true);
            System.out.println("baaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }
        System.out.println(navigationList.size() + " size3333333333333333333333333333333333333333333333333333");
        setForwardButtonDisabled(false);

        System.out.println("BACK BUTTON___________________________________________" + currentObject.getName());
        System.out.println(navigationList.size() + " size44444444444444444444444444444444444444444");
        if (selectedFSObject.getPath() == null) {
            parent.setPath("/");
            selectedFSObject.setPath("/");
        } else {
            parent.setPath(currentObject.getPath());
        }
        System.out.println(navigationList.size() + " size5555555555555555555555555555555555555555555555555");
        fsList = CMISService.getChildren(parent);
        System.out.println(navigationList.size() + " size666666666666666666666666666666666666666666666666");
        System.out.println(getNumber() + " number11111111111111111111111111111111111111111111111111111111");
    }

    public void forwardButton() {
        navigationList.remove(navigationList.size()-1);
        FSObject currentObject = navigationList.get(navigationList.size() - getNumber() + 2);
        setNumber(getNumber() - 1);
        if (getNumber() <= 2) {
            setForwardButtonDisabled(true);
            System.out.println("fffffffffaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }
        setBackButtonDisabled(false);


        System.out.println("FORWARD BUTTON________________________________________" + currentObject.getName());

        if (selectedFSObject.getPath() == null) {
            parent.setPath("/");
            selectedFSObject.setPath("/");
        } else {
            parent.setPath(currentObject.getPath());
        }
        fsList = CMISService.getChildren(parent);
        System.out.println(navigationList.size() + "size111111111111111111111111111111111111111111111111");
        System.out.println(getNumber() + "number11111111111111111111111111111111111111111111111111111111");
    }

    public void onRowSelect(SelectEvent event) {
        if(event == null) {
            logger.log(Level.INFO, "EVENT NULL");
        }
        this.selectedFSObject = (FSObject) event.getObject();
        if((selectedFSObject instanceof FSFolder) && (selectedFSObject != null)) {
            this.parent = (FSFolder) selectedFSObject;
        }
        logger.log(Level.INFO, "onRowSelect: " + selectedFSObject.getName());
    }

    public void onTreeSelect(SelectEvent event) {
        this.selectedNodes = (TreeNode) event.getObject();
        this.selectedFSObject = (FSObject) selectedNodes.getData();
        if((selectedFSObject instanceof FSFolder) && (selectedFSObject != null)) {
            this.parent = (FSFolder) selectedFSObject;
        }
        logger.log(Level.INFO, "onTreeSelect: " + selectedFSObject.getName());
    }

    public static int getNumber() {
        return number;
    }

    public static void setNumber(int number) {
        TreeBean.number = number;
    }

    public TreeNode getRoot() {
        return main;
    }

    public TreeNode getSelectedNode() {
        return selectedNodes;
    }

    public List<FSObject> getFsList() {
        return fsList;
    }

    public void setFsList(List<FSObject> fsList) {
        this.fsList = fsList;
    }

    public FSObject getSelectedFSObject() {
        return selectedFSObject;
    }

    public void setSelectedFSObject(FSObject sn) {
        if(sn != null)
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
}