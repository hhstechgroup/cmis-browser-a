package com.engagepoint.labs.core.service;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.FileAlreadyExistException;
import com.engagepoint.labs.core.models.exceptions.FolderAlreadyExistException;

import java.io.InputStream;
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
     * Create new folder in repository with folder name folderName
     * @param parent    parent folder for folderName folder
     * @param folderName
     * @return  created FSFolder object
     * @throws Exception if folder with this name exist in parent directory
     * or connection is fail
     */
    public FSFolder createFolder(FSFolder parent, String folderName) throws Exception;

    public FSFile createFile(FSFolder parent, String fileName, byte[] content, String mimeType);

    public FSFile edit(FSFile file, byte[] content, String mimeType);

    public boolean deleteFile(FSFile file);

    /**
     * Rename {@link FSFolder} folder in repository to name newName
     * @param folder   folder that is supposed to rename
     * @param newName  new name to folder
     * @return  renamed {@link FSFolder} object
     * @throws Exception
     */

    public FSFolder renameFolder(FSFolder folder, String newName) throws FolderAlreadyExistException;

    public boolean deleteFolder(FSFolder folder);

    public boolean deleteAllTree(FSFolder folder);


    public boolean hasChildFolder(FSFolder folder);

    public boolean hasChildren(FSFolder folder);

    public InputStream getInputStream(FSFile file);

    public FSFolder move(FSFolder source, FSFolder target);

    public void copyFolder(FSFolder folder, String name, String targetID);

    public FSFile getHistory(FSFile file);

}