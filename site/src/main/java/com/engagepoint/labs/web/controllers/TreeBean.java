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

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "treeBean")
@ApplicationScoped
public class TreeBean implements Serializable {
    private TreeNode main;
    private List<FSObject> fsList;
    private FSObject selectedFSObject;
    private TreeNode selectedNodes;
    private FSFolder parent = new FSFolder();
    private CMISService service = new CMISServiceImpl();

    public TreeBean() {
        FSFolder root = new FSFolder();
        root.setName("Root");
        parent.setPath("/");
        fsList = service.getChildren(parent);
        main = new DefaultTreeNode("Main", null);
        TreeNode node0 = new DefaultTreeNode(root, main);
        SubObjects(parent, node0);
    }

    private void SubObjects(FSFolder parent, TreeNode treenodeparent) {
        List<FSObject> children = service.getChildren(parent);
        for (FSObject i : children) {
            if (i instanceof FSFolder) {
                TreeNode treeNode = new DefaultTreeNode(i, treenodeparent);
                SubObjects((FSFolder) i, treeNode);
            }
        }
    }

    public TreeNode getRoot() {
        return main;
    }

    public TreeNode getSelectedNode() {
        return selectedNodes;
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

        fsList = service.getChildren(parent);
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
}