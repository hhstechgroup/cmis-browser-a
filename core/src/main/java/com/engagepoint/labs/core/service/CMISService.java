package com.engagepoint.labs.core.service;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;

import java.util.List;

/**
 * @author volodymyr.kozubal
 */
public interface CMISService {
    /**
     * Return a list children of our parent fsFolder folder
     *
     * @param fsFolder an parent folder which is supposed to get children
     * @return a List of children objects of parent object
     * @see FSFolder
     */
    public List<FSObject> getChildren(FSFolder fsFolder);

    /**
     * Return a root folder from our repository
     *
     * @return root folder
     * @see FSObject
     */
    public FSObject getRootFolder();

    public List<FSObject> getSubTreeObjects(FSFolder parent);

    public FSFolder createFolder(FSFolder parent, String name);

}
