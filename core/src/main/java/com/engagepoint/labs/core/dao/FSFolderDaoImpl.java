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
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FSFolderDaoImpl implements FSFolderDao {

    private Session session;
    private FSFileDao fsFileDao;
    private static Logger logger = Logger.getLogger(FSFolderDaoImpl.class.getName());


    public FSFolderDaoImpl() {
        fsFileDao = new FSFileDaoImpl();
    }

    @Override
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
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, folderName);
        Folder newFolder = cmisParent.createFolder(newFolderProps);
        FSFolder folder = new FSFolder();
        folder.setPath(newFolder.getPath());
        folder.setName(newFolder.getName());
        folder.setParent(parent);
        folder.setId(newFolder.getId());
        folder.setTypeId(newFolder.getType().getDisplayName());
        folder.setParentTypeId(newFolder.getType().getParentTypeId());
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
        for (CmisObject o : cmisChildren) {
            FSObject fsObject;
            if (o instanceof Folder) {
                fsObject = new FSFolder();
                fsObject.setPath(((Folder) o).getPath());
            } else {
                fsObject = new FSFile();
                fsObject.setMimetype(((Document) o).getContentStreamMimeType());
                fsObject.setPath(notRootFolder);
                fsObject.setSize(String.valueOf(((Document) o).getContentStreamLength() / 1024));
                ((FSFile) fsObject).setAbsolutePath(notRootFolder + "/" + o.getName());
            }
            fsObject.setParentTypeId(o.getType().getParentTypeId());
            fsObject.setCreatedBy(o.getCreatedBy());
            fsObject.setCreationTime(o.getCreationDate().getTime());
            fsObject.setLastModifiedBy(o.getLastModifiedBy());
            fsObject.setLastModifiedTime(o.getLastModificationDate().getTime());
            fsObject.setTypeId(o.getBaseType().getDisplayName());
            fsObject.setName(o.getName());
            fsObject.setId(o.getId());
            fsObject.setParent(parent);
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
    public boolean deleteAllTree(FSFolder folder) {
        Folder cmisFolder = (Folder) session.getObjectByPath(folder.getPath());
        cmisFolder.deleteTree(true, UnfileObject.DELETE, true);
        return true;
    }


    @Override
    public FSFolder getRoot() {
        Folder cmisRoot = session.getRootFolder();
        FSFolder root = new FSFolder();
        root.setName(cmisRoot.getName());
        root.setPath(cmisRoot.getPath());
        root.setId(cmisRoot.getId());
        root.setTypeId(cmisRoot.getBaseType().getDisplayName());
        return root;
    }

    @Override
    public int getMaxNumberOfRows(FSFolder parent){
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        ItemIterable<CmisObject> cmisChildren = cmisParent.getChildren();

        int total = (int) cmisChildren.getTotalNumItems();
        return total;
    }

    @Override
    public int getMaxNumberOfRowsByQuery(String query){
        if (!find(query).isEmpty()) {
        logger.log(Level.INFO, find(query).get(0).getName());
        }
        int total = (int) find(query).size();
        return total;
    }

    @Override
    public List<FSObject> getPageForLazySearchQuery(int first, int pageSize, String query) {

        return (List<FSObject>)find(query).subList(first, first + pageSize );
    }

    @Override
    public List<FSObject> getPageForLazy(FSFolder parent, int first, int pageSize){
        String notRootFolder = parent.getPath().equals("/") ? "" : parent.getPath();
        List<FSObject> children = new ArrayList<FSObject>();
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        OperationContext operationContext = session.createOperationContext();
        operationContext.setMaxItemsPerPage(pageSize);
        ItemIterable<CmisObject> childrenCmis = cmisParent.getChildren(operationContext);
        ItemIterable<CmisObject> cmisChildren = childrenCmis.skipTo(first).getPage();
        for (CmisObject o : cmisChildren) {
            FSObject fsObject;
            if (o instanceof Folder) {
                fsObject = new FSFolder();
                fsObject.setPath(((Folder) o).getPath());
            } else {
                fsObject = new FSFile();
                fsObject.setMimetype(((Document) o).getContentStreamMimeType());
                fsObject.setPath(notRootFolder);
                fsObject.setSize(String.valueOf(((Document) o).getContentStreamLength() / 1024));
                ((FSFile) fsObject).setAbsolutePath(notRootFolder + "/" + o.getName());
            }
            fsObject.setParentTypeId(o.getType().getParentTypeId());
            fsObject.setCreatedBy(o.getCreatedBy());
            fsObject.setCreationTime(o.getCreationDate().getTime());
            fsObject.setLastModifiedBy(o.getLastModifiedBy());
            fsObject.setLastModifiedTime(o.getLastModificationDate().getTime());
            fsObject.setTypeId(o.getType().getId());
            fsObject.setName(o.getName());
            fsObject.setId(o.getId());
            fsObject.setParent(parent);
            children.add(fsObject);
        }
        return children;
    }

    @Override
    public boolean hasChildFolder(FSFolder folder) {
        List<FSObject> children = getChildren(folder);
        if (!children.isEmpty()) {
            for (FSObject iterator : children) {
                if (iterator instanceof FSFolder) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasChildren(FSFolder folder) {
        List<FSObject> children = getChildren(folder);
        return !children.isEmpty();
    }

    @Override
    public FSFolder move(FSFolder source, FSFolder target) {
        Folder cmisFolder1 = (Folder) session.getObjectByPath(source.getPath());
        Folder cmisFolder2 = (Folder) session.getObjectByPath(target.getPath());
        Folder parent = cmisFolder1.getFolderParent();
        ObjectId objectId = new ObjectIdImpl(cmisFolder2.getId());
        ObjectId parObjectId = new ObjectIdImpl(parent.getId());
        cmisFolder1.move(parObjectId, objectId);
        return source;
    }

    @Override
    public void copyFolder(FSFolder folder, String name, String targetId) {
        Folder cmisFolderSource = (Folder) session.getObject(folder.getId());
        Folder cmisFolderTarget = (Folder) session.getObject(targetId);
        ItemIterable<CmisObject> cmisFolderSourceChildren = cmisFolderSource.getChildren();
        Map<String, String> newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, name);
        Folder copySourceFolder = cmisFolderTarget.createFolder(newFolderProps);

        for (CmisObject o : cmisFolderSourceChildren) {
            if (o instanceof Folder) {
                FSFolder fsFolder = new FSFolder();
                fsFolder.setPath(((Folder) o).getPath());
                fsFolder.setId(o.getId());
                copyFolder(fsFolder, o.getName(), copySourceFolder.getId());
            } else {
                ((Document) o).copy(new ObjectIdImpl(copySourceFolder.getId()));
            }
        }
    }

    @Override
    public List<FSObject> find(String query) {
        List<FSObject> files = new ArrayList<FSObject>();
        String myType = "cmis:document";
        ObjectType type = session.getTypeDefinition(myType);
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        parseFSFile(query, files, type, objectIdQueryName);
        String queryString;
        myType = "cmis:folder";
        type = session.getTypeDefinition(myType);
        objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        objectIdQueryName = objectIdPropDef.getQueryName();
        logger.log(Level.INFO, "before SELECT query " + query);
        parseFSFolder(query, files, type, objectIdQueryName);
        return files;
    }

    private void parseFSFolder(String query, List<FSObject> files, ObjectType type, String objectIdQueryName) {
        String queryString;
        queryString = "SELECT " + "*" + " FROM " + type.getQueryName() +" WHERE cmis:name LIKE '%" + query + "%'";
        ItemIterable<QueryResult> folderResults = session.query(queryString, false);
        logger.log(Level.INFO, "after SELECT query " + queryString);
        for (QueryResult qResult : folderResults) {
            FSFolder fsFolder = new FSFolder();
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Folder folder = (Folder) session.getObject(session.createObjectId(objectId));
            fsFolder.setPath(folder.getPath());
            fsFolder.setName(folder.getName());
            logger.log(Level.INFO, "folder get name " + folder.getName());
            fsFolder.setId(folder.getId());
            fsFolder.setTypeId(folder.getType().getDisplayName());
            fsFolder.setParentTypeId(folder.getType().getParentTypeId());
            files.add(fsFolder);
            logger.log(Level.INFO, "Result from query:______________" + fsFolder.getName());
        }
    }

    private void parseFSFile(String query, List<FSObject> files, ObjectType type, String objectIdQueryName) {
        String queryString = "SELECT " + "*" + " FROM " + type.getQueryName() +" WHERE cmis:name LIKE '%" + query + "%'";
        ItemIterable<QueryResult> fileResult = session.query(queryString, false);
        for (QueryResult qResult : fileResult) {
            FSFile fsFile = new FSFile();
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Document doc = (Document) session.getObject(session.createObjectId(objectId));
            fsFile.setName(doc.getName());
            fsFile.setId(doc.getId());
            fsFile.setTypeId(doc.getType().getId());
            fsFile.setParentTypeId(doc.getType().getParentTypeId());
            fsFile.setCreatedBy(doc.getCreatedBy());
            fsFile.setCreationTime(doc.getCreationDate().getTime());
            fsFile.setLastModifiedBy(doc.getLastModifiedBy());
            fsFile.setLastModifiedTime(doc.getLastModificationDate().getTime());
            fsFile.setAbsolutePath(doc.getPaths().get(0));
            files.add(fsFile) ;
        }
    }
}
