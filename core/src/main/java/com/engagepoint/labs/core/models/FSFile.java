package com.engagepoint.labs.core.models;

/**
 * User: vitaliy.vasilenko
 * Date: 6/17/13
 * Time: 2:51 PM
 */
public class FSFile extends FSObject {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getType() {
        return "Document";
    }

    @Override
    public String getIcon(){
        return "document.png";
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FSFile)){ return false;
        }
        FSFile fsFile = (FSFile) o;

        if (content != null ? !content.equals(fsFile.content) : fsFile.content != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }
}
