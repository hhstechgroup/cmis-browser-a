package com.engagepoint.labs.core.service;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;

import java.util.List;

/**
 * @author volodymyr.kozubal  <volodymyr.kozubal@engagepoint.com>
 */
public interface CMISService {
    /**
     * Return  max value  of page number
     *
     * @param parent parent object, pageNumber, numbersOfRows for right pagination
     * @return List of FSObjects on the page
     */
    public int getMaxNumberOfPage(FSFolder parent, int numberOfRows);

    /**
     * Return a list children of our parent fsFolder folder
     *
     * @param fsFolder an parent folder which is supposed to get children
     * @return a List of children objects of parent object
     * @see FSFolder
     */
    public List<FSObject> getChildren(FSFolder fsFolder);

    /**
     * Return list of FSObject on actually page
     *
     * @param parent parent object, pageNumber, numbersOfRows for right pagination
     * @return List of FSObjects on the page
     */
    public List<FSObject> getPage(FSFolder parent, int pageNumber, int numberOfRows);

    /**
     * Return a root folder from our repository
     *
     * @return root folder
     * @see FSObject
     */
    public FSFolder getRootFolder();

    /**
     * Get all nodes from parent object subtree
     *
     * @param parent parent object
     * @return List of all file and folder
     */
    public List<FSObject> getSubTreeObjects(FSFolder parent);

    /**
     * get content of the file
     *
     * @param file FSFile object which it is supposed to get content
     * @return content of the file
     */
    public String getContent(FSFile file);

    /**
     * Create new folder in repository with folder name folderName
     * @param parent    parent folder for folderName folder
     * @param folderName
     * @return  created FSFolder object
     * @throws Exception if folder with this name exist in parent directory
     * or connection is fail
     */
    public FSFolder createFolder(FSFolder parent, String folderName) throws Exception;

    public FSFile createFile(FSFolder parent, String fileName, String content);

    public FSFile renameFile(FSFile file, String newName)throws Exception;

    public boolean deleteFile(FSFile file);

    /**
     * Rename {@link FSFolder} folder in repository to name newName
     * @param folder   folder that is supposed to rename
     * @param newName  new name to folder
     * @return  renamed {@link FSFolder} object
     * @throws Exception
     */

    public FSFolder renameFolder(FSFolder folder, String newName)throws Exception;

    public boolean deleteFolder(FSFolder folder);

    public boolean deleteAllTree(FSFolder folder);

    /**
     * Say does folder has any clild folder
     *
     * @param folder  folder which is explored to find any child folder
     * @return  true if has more than one child folder
     */
    public boolean hasChildFolder(FSFolder folder);

    public boolean hasChildren(FSFolder folder);

}
