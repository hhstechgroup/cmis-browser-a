package com.engagepoint.labs.core.service;

import com.engagepoint.labs.core.dao.*;
import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.BrowserRuntimeException;
import com.engagepoint.labs.core.models.exceptions.FolderAlreadyExistException;
import com.engagepoint.labs.core.models.exceptions.FolderNotFoundException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author volodymyr.kozubal
 */

public class CMISServiceImpl implements CMISService {

    private FSFolderDao fsFolderDao;
    private static Logger logger = Logger.getLogger(CMISServiceImpl.class.getName());

    private static CMISServiceImpl service = null;

    private CMISServiceImpl() throws BaseException{
        fsFolderDao = new FSFolderDaoImpl();
        fsFolderDao.setSession(ConnectionFactory.getSession());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FSObject> getChildren(FSFolder fsFolder) throws BaseException {
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
    public FSFolder createFolder(FSFolder parent, String folderName) throws BaseException {
        return fsFolderDao.create(parent, folderName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FSFile createFile(FSFolder parent, String fileName, byte[] content, String mimeType) throws BaseException {
        return fsFolderDao.getFsFileDao().create(parent, fileName, content, mimeType);
    }

    @Override
    public FSFile edit(FSFile file, byte[] content, String mimeType) throws BaseException {
        return fsFolderDao.getFsFileDao().edit(file, content, mimeType);
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
    public boolean deleteFolder(FSFolder folder) {
        return fsFolderDao.delete(folder);
    }

    @Override
    public boolean deleteAllTree(FSFolder folder) throws FolderNotFoundException {
        return fsFolderDao.deleteAllTree(folder);
    }

    public static CMISServiceImpl getService() throws BaseException {
        if (service == null) {
            service = new CMISServiceImpl();
        }
        return service;
    }

    @Override
    public List<FSObject> getPageForLazy(FSFolder parent, int first, int pageSize) throws BaseException {
        return fsFolderDao.getPageForLazy(parent, first, pageSize);
    }

    @Override
    public int getMaxNumberOfRows(FSFolder parent){
        return fsFolderDao.getMaxNumberOfRows(parent);
    }

    @Override
    public int getMaxNumberOfRowsByQuery(String query){
        return fsFolderDao.getMaxNumberOfRowsByQuery(query);
    }

    @Override
    public int getMaxNumberOfRowsByQuery(Map<Integer, Object> query){
        logger.log(Level.INFO, "============BEFORE FIND======");
        int total = fsFolderDao.find(query).size();
        return total;
    }

    @Override
    public List<FSObject> getPageForLazySearchQuery(int first, int pageSize, String query) {
        return fsFolderDao.getPageForLazySearchQuery(first, pageSize, query);
    }

    @Override
    public List<FSObject> getPageForLazySearchQuery(int first, int pageSize, Map<Integer, Object> query) {

        return fsFolderDao.find(query).subList(first, first + pageSize );
    }

    @Override
    public boolean hasChildFolder(FSFolder folder) throws BaseException {
        return fsFolderDao.hasChildFolder(folder);
    }

    @Override
    public boolean hasChildren(FSFolder folder) throws BaseException {
        return fsFolderDao.hasChildFolder(folder);
    }

    @Override
    public InputStream getInputStream(FSFile file) throws BaseException {
        return fsFolderDao.getFsFileDao().getInputStream(file);
    }
    @Override
    public FSFolder move(FSFolder source, FSFolder target) throws BrowserRuntimeException {
        return fsFolderDao.move(source,target);
    }

    @Override
    public void copyFolder(FSFolder folder, String name, String targetID) throws FolderAlreadyExistException {
        fsFolderDao.copyFolder(folder, name, targetID);
    }

    @Override
    public FSFile getHistory(FSFile file) {
        return fsFolderDao.getFsFileDao().getHistory(file);
    }

    @Override
    public List<FSObject> find(String query) {
        return fsFolderDao.find(query);
    }

    @Override
    public void copyFile(String fileId, String name, String targetId) throws BaseException {
        fsFolderDao.getFsFileDao().copy(fileId, name, targetId);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public FSFolder renameFolder(FSFolder folder, String newName) throws BaseException {
        return fsFolderDao.rename(folder, newName);
    }

}