package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import org.apache.chemistry.opencmis.client.api.Session;

import java.io.InputStream;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 2:52 PM
 */
public interface FSFileDao {

    /**
     * Connect to repository
     *
     * @param session - session
     */
    public void setSession(Session session);

    /**
     * Create new file in parent folder
     *
     * @param parent - folder in which you want create new file
     * @param fileName - name of file which you want create
     * @param content - content of file
     * @return created file, FSFile type
     */
    public FSFile create(FSFolder parent, String fileName, byte[] content, String mimeType) throws BaseException;

    /**
     * Method that will delete file from repository
     *
     * @param file - file which you want to delete
     * @return <b>true</b> if deleted
     */
    public boolean delete(FSFile file);

    public FSFile edit(FSFile file, byte[] content, String mimeType) throws BaseException;

    public InputStream getInputStream(FSFile file) throws BaseException;

    /**
     * Method that will copy file
     *
     * @param id - file which you want to copy
     * @param newName - new name of file
     * @param targetId - id of folder in which we want to copy
     * @return <b>true</b> if copied
     */
    public boolean copy(String id, String newName, String targetId) throws BaseException;

    public FSFile getHistory(FSFile file);
}
