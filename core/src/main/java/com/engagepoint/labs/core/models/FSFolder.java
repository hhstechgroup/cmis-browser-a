package com.engagepoint.labs.core.models;


import java.util.List;

/**
 * User: vitaliy.vasilenko
 * Date: 6/17/13
 * Time: 2:51 PM
 */
public class FSFolder extends FSObject {

    private List<FSObject> children;

    public List<FSObject> getChildren() {
        return this.children;
    }

    public void setChildren(List<FSObject> children) {
        this.children = children;
    }

    @Override
    public String getType() {
        return "Folder";
    }

    @Override
    public String getIcon(){
        return "folder.png";
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FSFolder folder = (FSFolder) o;

        if (children != null ? !children.equals(folder.children) : folder.children != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }
}