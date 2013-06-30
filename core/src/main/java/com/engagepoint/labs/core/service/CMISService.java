package com.engagepoint.labs.core.service;

import com.engagepoint.labs.core.models.FSFile;
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
    public FSFolder getRootFolder();

    /**
     * Get all nodes from parent object subtree
     * @param parent parent object
     * @return List of all file and folder
     */
    public List<FSObject> getSubTreeObjects (FSFolder parent);

    /**
     * get content of the file
     * @param file FSFile object which it is supposed to get content
     * @return content of the file
     */
    public String getContent(FSFile file);

    public FSFolder createFolder  (FSFolder parent, String folderName);

    public FSFile createFile ( FSFolder parent, String fileName, String content );

    public FSFile renameFile (FSFile file, String newName);

    public String getFileContent(FSFile file);

    public boolean deleteFile (FSFile file);

    public FSFolder renameFolder(FSFolder folder, String newName);

    public boolean deleteFolder(FSFolder folder);

    public boolean deleteAllTree(FSFolder folder);
}