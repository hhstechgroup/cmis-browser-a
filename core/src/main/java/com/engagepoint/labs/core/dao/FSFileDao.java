package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
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
     * @param parent   - folder in which you want create new file
     * @param fileName - name of file which you want create
     * @param content  - content of file
     * @return created file, FSFile type
     */
    public FSFile create(FSFolder parent, String fileName, byte[] content, String mimeType);

    /**
     * Method that will rename file
     *
     * @param file    - file which you want rename
     * @param newName - new name of file
     * @return renamed file
     */
    public FSFile rename(FSFile file, String newName);

    /**
     * Method that will delete file from repository
     *
     * @param file - file which you want to delete
     * @return <b>true</b> if deleted
     */
    public boolean delete(FSFile file);

    public FSFile edit(FSFile file, byte[] content, String mimeType);

    public InputStream getInputStream(FSFile file);

    public void copy(String id, String targetId);

    public FSFile getHistory(FSFile file);
}
