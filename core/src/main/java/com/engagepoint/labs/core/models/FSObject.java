package com.engagepoint.labs.core.models;

/**
 * User: vitaliy.vasilenko
 * Date: 6/17/13
 * Time: 12:42 PM
 */
public abstract class FSObject {

    private String type;
    private String path;
    private FSFolder parent;
    private String name;
    private String id;
    private String absolutePath;
    private String icon;


    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FSFolder getParent() {
        return parent;
    }

    public void setParent(FSFolder parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FSObject)) return false;

        FSObject fsObject = (FSObject) o;

        if (id != null ? !id.equals(fsObject.id) : fsObject.id != null) return false;
        if (name != null ? !name.equals(fsObject.name) : fsObject.name != null) return false;
        if (parent != null ? !parent.equals(fsObject.parent) : fsObject.parent != null) return false;
        if (path != null ? !path.equals(fsObject.path) : fsObject.path != null) return false;
        if (type != null ? !type.equals(fsObject.type) : fsObject.type != null) return false;

        return true;
    }

    private boolean getHashCode(FSObject fsObject) {
        if (id != null ? !id.equals(fsObject.id) : fsObject.id != null) {
            return true;
        }
        if (name != null ? !name.equals(fsObject.name) : fsObject.name != null){
            return true;
        }
        if (parent != null ? !parent.equals(fsObject.parent) : fsObject.parent != null) {
            return true;
        }
        if (path != null ? !path.equals(fsObject.path) : fsObject.path != null){
            return true;
        }
        if (type != null ? !type.equals(fsObject.type) : fsObject.type != null) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
