package com.engagepoint.labs.core.models;

import java.util.Arrays;

/**
 * User: vitaliy.vasilenko
 * Date: 6/17/13
 * Time: 2:51 PM
 */
public class FSFile extends FSObject {

    private String mimetype;
    private byte[] content;

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String getType() {
        return super.getType();
    }

    @Override
    public void setType(String type) {
        super.setType(type);
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        FSFile fsFile = (FSFile) o;

        if (!Arrays.equals(content, fsFile.content)) {
            return false;
        }
        if (mimetype != null ? !mimetype.equals(fsFile.mimetype) : fsFile.mimetype != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mimetype != null ? mimetype.hashCode() : 0);
        result = 31 * result + (content != null ? Arrays.hashCode(content) : 0);
        return result;
    }
}
