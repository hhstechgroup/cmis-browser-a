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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageState)) return false;

        PageState pageState = (PageState) o;

        if (currentPage != pageState.currentPage) return false;
        if (parentpath != null ? !parentpath.equals(pageState.parentpath) : pageState.parentpath != null) return false;
        if (selectedNode != null ? !selectedNode.equals(pageState.selectedNode) : pageState.selectedNode != null)
            return false;
        if (selectedObject != null ? !selectedObject.equals(pageState.selectedObject) : pageState.selectedObject != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = currentPage;
        result = 31 * result + (selectedNode != null ? selectedNode.hashCode() : 0);
        result = 31 * result + (selectedObject != null ? selectedObject.hashCode() : 0);
        result = 31 * result + (parentpath != null ? parentpath.hashCode() : 0);
        return result;
    }
}
