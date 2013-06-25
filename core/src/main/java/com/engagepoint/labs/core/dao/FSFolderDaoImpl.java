package com.engagepoint.labs.core.dao;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 1:41 PM
 */

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FSFolderDaoImpl implements FSFolderDao {

    private Session session;
    private FSFileDao fsFileDao;

    public FSFolderDaoImpl() {
        fsFileDao = new FSFileDaoImpl();
    }

    public FSFileDao getFsFileDao() {
        return fsFileDao;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
        fsFileDao.setSession(session);
    }

    @Override
    public FSFolder create(FSFolder parent, String folderName) {
        Map<String, String> newFolderProps = new HashMap<String, String>();
        String path = parent.getPath();
        Folder cmisParent = (Folder) session.getObjectByPath(path);
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, folderName);
        Folder newFolder = cmisParent.createFolder(newFolderProps);
        FSFolder folder = new FSFolder();
        folder.setPath(newFolder.getPath());
        folder.setName(newFolder.getName());
        folder.setType("cmis:folder");
        folder.setParent(parent);
        folder.setId(newFolder.getId());
        folder.setChildren(null);
        return folder;
    }

    @Override
    public FSFolder rename(FSFolder folder, String newName) {
        Folder cmisFolder = (Folder) session.getObjectByPath(folder.getPath());
        Map<String, String> newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.NAME, newName);
        cmisFolder.updateProperties(newFolderProps, true);
        folder.setName(cmisFolder.getName());
        folder.setPath(cmisFolder.getPath());
        return folder;
    }

    @Override
    public List<FSObject> getChildren(FSFolder parent) {
        String notRootFolder = parent.getPath().equals("/") ? "" : parent.getPath();
        List<FSObject> children = new ArrayList<FSObject>();
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        ItemIterable<CmisObject> cmisChildren = cmisParent.getChildren();
        for(CmisObject o : cmisChildren) {
            FSObject fsObject;
            if(o instanceof Folder){
                fsObject = new FSFolder();
                fsObject.setName(o.getName());
                fsObject.setPath(((Folder) o).getPath());
                fsObject.setType(o.getType().getDisplayName());
                fsObject.setId(o.getId());
                fsObject.setParent(parent);
            } else {
                fsObject = new FSFile();
                fsObject.setName(o.getName());
                fsObject.setId(o.getId());
                fsObject.setType(o.getType().getDisplayName());
                fsObject.setPath(notRootFolder);
                fsObject.setAbsolutePath(notRootFolder + "/" + o.getName());
                fsObject.setParent(parent);
            }
            children.add(fsObject);
        }
        return children;
    }

    @Override
    public boolean delete(FSFolder folder) {
        Folder cmisFolder = (Folder) session.getObjectByPath(folder.getPath());
        cmisFolder.delete(true);
        return true;
    }

    @Override
    public FSFolder getRoot() {
        Folder cmisRoot = session.getRootFolder();
        FSFolder root = new FSFolder();
        root.setName(cmisRoot.getName());
        root.setPath(cmisRoot.getPath());
        root.setId(cmisRoot.getId());
        root.setType(cmisRoot.getType().getDisplayName());
        return root;
    }
}
