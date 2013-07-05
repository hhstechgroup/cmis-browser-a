package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSObject;
import org.primefaces.model.TreeNode;


/**
 * Created with IntelliJ IDEA.
 * User: r.reznichenko
 * Date: 7/4/13
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class PageState {

    private int currentPage;
    private TreeNode selectedNode;
    private FSObject selectedObject;
    private String parentpath;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public FSObject getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(FSObject selectedObject) {
        this.selectedObject = selectedObject;
    }

    public String getParentpath() {
        return parentpath;
    }

    public void setParentpath(String parentpath) {
        this.parentpath = parentpath;
    }
}
