package com.engagepoint.labs.web.controllers;

/**
 * Created with IntelliJ IDEA.
 * User: bogdan.ezapenkin
 * Date: 6/17/13
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean(name = "treeBean")
@ApplicationScoped
public class TreeBean implements Serializable {
    private TreeNode main;
    private List<FSObject> fsList;
    private FSObject selectedFSObject;
    private TreeNode selectedNodes;
    private FSFolder parent = new FSFolder();
    private CMISService CMISService = new CMISServiceImpl();
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());
    private String name;
    private String type;


    public TreeBean() {
        FSFolder root = new FSFolder();
        root.setName("Root");
        parent.setPath("/");
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
        this.selectedNodes = selectedNodes;
        selectedFSObject = (FSObject) getSelectedNode().getData();
        if (selectedFSObject.getPath() == null) {
            parent.setPath("/");
            selectedFSObject.setPath("/");
        } else {
            parent.setPath(selectedFSObject.getPath());
        }

        fsList = CMISService.getChildren(parent);
    }

 //   public void deleteNode() {
//        selectedNodes.getChildren().clear();
//        selectedNodes.getParent().getChildren().remove(selectedNodes);
//        selectedNodes.setParent(null);
//
//        selectedNodes = null;
//    }

    /**
     * Create new folder with name {@link this#name} and type {@link this#type}
     * in {@link this#selectedFSObject} parent directory
     */
    public void createFolder(ActionEvent event) {
        try {
            CMISService.createFolder((FSFolder) selectedFSObject, name);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception: ", ex);
           //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Connection Fail", event.toString());

            //FacesContext.getCurrentInstance().addMessage(null, message);
        }

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
        this.selectedFSObject = sn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}