package com.engagepoint.labs.core.service;

import com.engagepoint.labs.core.dao.ConnectionFactory;
import com.engagepoint.labs.core.dao.FSFolderDao;
import com.engagepoint.labs.core.dao.FSFolderDaoImpl;
import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author volodymyr.kozubal
 */

public class CMISServiceImpl implements CMISService {

    private FSFolderDao fsFolderDao;
    private static Logger logger = Logger.getLogger(CMISServiceImpl.class.getName());

    private static CMISServiceImpl service = null;

    private CMISServiceImpl() {
        fsFolderDao = new FSFolderDaoImpl();
        fsFolderDao.setSession(ConnectionFactory.getSession());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FSObject> getChildren(FSFolder fsFolder) {
        return fsFolderDao.getChildren(fsFolder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FSFolder getRootFolder() {
        return fsFolderDao.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FSFolder createFolder(FSFolder parent, String folderName) {
        return fsFolderDao.create(parent, folderName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FSFile createFile(FSFolder parent, String fileName, byte[] content, String mimeType) {
        return fsFolderDao.getFsFileDao().create(parent, fileName, content, mimeType);
    }

    @Override
    public FSFile edit(FSFile file, byte[] content, String mimeType){
        return fsFolderDao.getFsFileDao().edit(file, content, mimeType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FSFile renameFile(FSFile file, String newName) {
        return fsFolderDao.getFsFileDao().rename(file, newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFile(FSFile file) {
        return fsFolderDao.getFsFileDao().delete(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FSFolder renameFolder(FSFolder folder, String newName) {
        return fsFolderDao.rename(folder, newName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteFolder(FSFolder folder) {
        return fsFolderDao.delete(folder);
    }

    @Override
    public boolean deleteAllTree(FSFolder folder) {
        return fsFolderDao.deleteAllTree(folder);
    }

    public static CMISServiceImpl getService() {
        if (service == null) {
            service = new CMISServiceImpl();
        }
        return service;
    }

    @Override
    public List<FSObject> getPage(FSFolder parent, int pageNumber, int numberOfRows) {
        return fsFolderDao.getPage(parent, pageNumber - 1, numberOfRows);
    }

    @Override
    public int getMaxNumberOfPage(FSFolder parent, int numberOfRows) {
        return fsFolderDao.getMaxNumberOfPage(parent, numberOfRows);
    }

    @Override
    public boolean hasChildFolder(FSFolder folder) {
        return fsFolderDao.hasChildFolder(folder);
    }

    @Override
    public boolean hasChildren(FSFolder folder) {
        return fsFolderDao.hasChildFolder(folder);
    }

    @Override
    public InputStream getInputStream(FSFile file){
        return fsFolderDao.getFsFileDao().getInputStream(file);
    }
    @Override
    public FSFolder move(FSFolder source, FSFolder target){
        return fsFolderDao.move(source,target);
    }
}