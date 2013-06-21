package com.engagepoint.labs.core.service;

import com.engagepoint.labs.core.dao.*;
import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author volodymyr.kozubal
 */

public class CMISServiceImpl implements CMISService {
    private FSFolderDao fsFolderDao;
    private ConnectionFactory connect;
    private List<FSObject> list;

    public CMISServiceImpl() {
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
    public FSObject getRootFolder() {
        return fsFolderDao.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContent(FSFile file) {
        return fsFolderDao.getFsFileDao().getContent(file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FSObject> getSubTreeObjects(FSFolder parent) {

        list = new ArrayList<FSObject>();
        SubObjects(parent);
        return list;
    }

    /**
     * additional method for getSubTreeObjects method
     * is invoked recusively for all subnodes
     *
     * @param parent FSFolder
     * @method getSubTreeObjects
     */
    private void SubObjects(FSFolder parent) {

        List<FSObject> children = getChildren(parent);
        list.addAll(children);
        for (FSObject i : children) {
            if (i instanceof FSFolder)

                SubObjects((FSFolder) i);
        }

    }


}