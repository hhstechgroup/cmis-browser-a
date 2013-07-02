package com.engagepoint.labs.core.dao;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 2:53 PM
 */

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import org.apache.chemistry.opencmis.client.api.Session;

import java.util.List;

public interface FSFolderDao {

    /**
     * Connect to repository
     *
     * @param session - session
     */
    public void setSession(Session session);

    /**
     * Create new folder in parent folder
     *
     * @param parent     - folder in which you want create new folder
     * @param folderName - name of folder which you want create
     * @return created folder, FSFolder type
     */
    public FSFolder create(FSFolder parent, String folderName);

    /**
     * Method that will rename folder
     *
     * @param folder  - folder which you want rename
     * @param newName - new name of folder
     * @return renamed folder
     */
    public FSFolder rename(FSFolder folder, String newName);

    /**
     * Get all children from parent folder
     *
     * @param parent - folder in which you want get children
     * @return - list of FSObject files
     */
    public List<FSObject> getChildren(FSFolder parent);

    /**
     * Method that will delete folder from repository
     *
     * @param folder - folder which you want to delete
     * @return <b>true</b> if deleted
     */
    public boolean delete(FSFolder folder);

    public boolean deleteAllTree(FSFolder folder);

    /**
     * @return root folder
     */
    public FSFolder getRoot();

    /**
     * @return reference to FSFileDao
     */
    public FSFileDao getFsFileDao();

    public int getMaxNumberOfPage(FSFolder parent, int numberOfRows);

    public List<FSObject> getPage(FSFolder parent, int pageNumber, int numberOfRows);

}