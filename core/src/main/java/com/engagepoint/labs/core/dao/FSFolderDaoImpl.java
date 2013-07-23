package com.engagepoint.labs.core.dao;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 1:41 PM
 */

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.*;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.BrowserRuntimeException;
import com.engagepoint.labs.core.models.exceptions.ConnectionException;
import com.engagepoint.labs.core.models.exceptions.FolderAlreadyExistException;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FSFolderDaoImpl implements FSFolderDao {

    private Session session;
    private FSFileDao fsFileDao;
    private static Logger logger = Logger.getLogger(FSFolderDaoImpl.class.getName());

    private Map<Integer, String> searchAdvancedParametrs;

    public FSFolderDaoImpl() {
        fsFileDao = new FSFileDaoImpl();

        searchAdvancedParametrs = new HashMap<Integer, String>();
        searchAdvancedParametrs.put(0, "SELECT * FROM ? WHERE ");
        searchAdvancedParametrs.put(1, " cmis:name LIKE ? ");
        searchAdvancedParametrs.put(2, "");
        searchAdvancedParametrs.put(3, " cmis:contentStreamFileName LIKE ? ");
        searchAdvancedParametrs.put(4, " CONTAINS(?) ");
        searchAdvancedParametrs.put(5, "");
        searchAdvancedParametrs.put(6, " cmis:lastModificationDate >=  TIMESTAMP ? ");
        searchAdvancedParametrs.put(7, " cmis:lastModificationDate <=  TIMESTAMP ? ");
        searchAdvancedParametrs.put(8, " cmis:contentStreamLength >= \"");
        searchAdvancedParametrs.put(9, " cmis:contentStreamLength <= \"");
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
    public FSFolder create(FSFolder parent, String folderName) throws BaseException {
        FSFolder folder = null;
        try {
            Map<String, String> newFolderProps = new HashMap<String, String>();
            Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
            newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            newFolderProps.put(PropertyIds.NAME, folderName);
            Folder newFolder = cmisParent.createFolder(newFolderProps);
            folder = new FSFolder();
            folder.setPath(newFolder.getPath());
            folder.setName(newFolder.getName());
            folder.setParent(parent);
            folder.setId(newFolder.getId());
            folder.setTypeId(newFolder.getType().getDisplayName());
            folder.setParentTypeId(newFolder.getType().getParentTypeId());
        } catch (CmisNameConstraintViolationException ex) {
            throw new FolderAlreadyExistException(ex.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
        return folder;
    }


    @Override
    public List<FSObject> getChildren(FSFolder parent) throws BaseException /*throws BaseException*/ {
        String notRootFolder = parent.getPath().equals("/") ? "" : parent.getPath();
        List<FSObject> children = new ArrayList<FSObject>();
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        ItemIterable<CmisObject> cmisChildren = cmisParent.getChildren();
        try {
            for (CmisObject o : cmisChildren) {
                FSObject fsObject;
                if (o instanceof Folder) {
                    fsObject = new FSFolder();
                    fsObject.setPath(((Folder) o).getPath());
                } else {
                    fsObject = new FSFile();
                    fsObject.setMimetype(((Document) o).getContentStreamMimeType());
                    ((FSFile) fsObject).setVersionable(((DocumentType) (o.getType())).isVersionable());
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
        } catch (CmisObjectNotFoundException e) {
            throw new ConnectionException(e.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
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
    public boolean deleteAllTree(FSFolder folder) throws FolderNotFoundException {
        try {
            Folder cmisFolder = (Folder) session.getObjectByPath(folder.getPath());
            cmisFolder.deleteTree(true, UnfileObject.DELETE, true);
        } catch (CmisObjectNotFoundException e) {
            throw new FolderNotFoundException("Folder not found!");
        }
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
    public int getMaxNumberOfRows(FSFolder parent) {
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        ItemIterable<CmisObject> cmisChildren = cmisParent.getChildren();

        int total = (int) cmisChildren.getTotalNumItems();
        return total;
    }

    @Override
    public int getMaxNumberOfRowsByQuery(String query) {
        if (!find(query).isEmpty()) {
            logger.log(Level.INFO, find(query).get(0).getName());
        }
        int total = (int) find(query).size();
        return total;
    }

    @Override
    public List<FSObject> getPageForLazySearchQuery(int first, int pageSize, String query) {

        return (List<FSObject>) find(query).subList(first, first + pageSize);
    }

    @Override
    public List<FSObject> getPageForLazy(FSFolder parent, int first, int pageSize) throws BaseException {
        String notRootFolder = parent.getPath().equals("/") ? "" : parent.getPath();
        List<FSObject> children = new ArrayList<FSObject>();
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());

        OperationContext operationContext = session.createOperationContext();
        operationContext.setMaxItemsPerPage(pageSize);
        ItemIterable<CmisObject> childrenCmis = cmisParent.getChildren(operationContext);
        ItemIterable<CmisObject> cmisChildren = childrenCmis.skipTo(first).getPage();
        try {
            for (CmisObject o : cmisChildren) {
                FSObject fsObject;
                if (o instanceof Folder) {
                    fsObject = new FSFolder();
                    fsObject.setPath(((Folder) o).getPath());
                } else {
                    fsObject = new FSFile();
                    fsObject.setMimetype(((Document) o).getContentStreamMimeType());
                    fsObject.setPath(notRootFolder);
                    ((FSFile) fsObject).setVersionable(((DocumentType) (o.getType())).isVersionable());
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
        } catch (CmisObjectNotFoundException e) {
            throw new ConnectionException(e.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
        return children;
    }

    @Override
    public boolean hasChildFolder(FSFolder folder) throws BaseException {
        List<FSObject> children = null;
        try {
            children = getChildren(folder);
        } catch (ConnectionException e) {
            throw new ConnectionException(e.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
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
    public FSFolder rename(FSFolder folder, String newName) throws BaseException {
        try {
            Folder cmisFolder = (Folder) session.getObjectByPath(folder.getPath());
            Map<String, String> newFolderProps = new HashMap<String, String>();
            newFolderProps.put(PropertyIds.NAME, newName);
            cmisFolder.updateProperties(newFolderProps);
            folder.setName(cmisFolder.getName());
            folder.setPath(cmisFolder.getPath());
        } catch (CmisNameConstraintViolationException ex) {
            throw new FolderAlreadyExistException("Folder already exist");
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }

        return folder;
    }

    @Override
    public boolean hasChildren(FSFolder folder) throws BaseException {
        List<FSObject> children = null;
        try {
            children = getChildren(folder);
        } catch (ConnectionException e) {
            throw new ConnectionException(e.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
        return !children.isEmpty();
    }

    @Override
    public FSFolder move(FSFolder source, FSFolder target) throws BrowserRuntimeException {
        Folder cmisFolder1 = (Folder) session.getObjectByPath(source.getPath());
        Folder cmisFolder2 = (Folder) session.getObjectByPath(target.getPath());
        Folder parent = cmisFolder1.getFolderParent();
        ObjectId objectId = new ObjectIdImpl(cmisFolder2.getId());
        ObjectId parObjectId = new ObjectIdImpl(parent.getId());
        try {
            cmisFolder1.move(parObjectId, objectId);
        } catch (CmisRuntimeException e) {
            throw new BrowserRuntimeException("Cant move folder!");
        }
        return source;
    }

    @Override
    public void copyFolder(FSFolder folder, String name, String targetId) throws FolderAlreadyExistException {
        Folder cmisFolderSource = (Folder) session.getObject(folder.getId());
        Folder cmisFolderTarget = (Folder) session.getObject(targetId);
        ItemIterable<CmisObject> cmisFolderSourceChildren = cmisFolderSource.getChildren();
        Map<String, String> newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, name);

        Folder copySourceFolder = null;
        try {
            copySourceFolder = cmisFolderTarget.createFolder(newFolderProps);
        } catch (CmisNameConstraintViolationException e) {
            throw new FolderAlreadyExistException("Folder already exist!");
        }

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

    /**
     * Return all results from searching in repository by query
     *
     * @param query - part of searching word for query
     * @return List files
     */
    @Override
    public List<FSObject> find(String query) {
        List<FSObject> files = new ArrayList<FSObject>();
        parseFSFile(query, files);
        parseFSFolder(query, files);
        return files;
    }

    @Override
    public List<FSObject> find(Map<Integer, Object> query) {
        List<FSObject> files = new LinkedList<FSObject>();
        String myType = (String)query.get(0);
        logger.log(Level.INFO, "=========" + myType);
        ObjectType type = session.getTypeDefinition(myType);
        logger.log(Level.INFO, "====!=====");
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        logger.log(Level.INFO, "====!1=====");
        String objectIdQueryName = objectIdPropDef.getQueryName();
        logger.log(Level.INFO, "====!11=====");
        String queryString = getQuery(query);
        ItemIterable<QueryResult> Results = session.query(queryString, false);
        if(myType.equals("cmis:document")){
            parseFSFile(files, objectIdQueryName, Results);
        } else {
            parseFSFolder(files, objectIdQueryName, Results);
        }
//        myType = "cmis:folder";
//        type = session.getTypeDefinition(myType);
//        objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
//        objectIdQueryName = objectIdPropDef.getQueryName();
////        logger.log(Level.INFO, "============DAOOOOOOO!======" );
//        queryString = getQuery(query, type);
////        logger.log(Level.INFO, "============DAOOOOOOO!!======" );
//        ItemIterable<QueryResult> folderResults = session.query(queryString, false);
////        logger.log(Level.INFO, "============DAOOOOOOO!!!======" );
//        parseFSFolder(files, objectIdQueryName, folderResults);
////        logger.log(Level.INFO, "============DAOOOOOOO!!!!======" );
        return files;
    }

    /**
     * Find all cmis folders from searching in repository by query
     *
     * @param query - part of searching word for query
     * @param files - List for results
     * @return List files
     */
    public void parseFSFolder(String query, List<FSObject> files) {
        ObjectType type = session.getTypeDefinition("cmis:folder");
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        String queryString = getFullQuery(query, type);
        ItemIterable<QueryResult> folderResults = session.query(queryString, false);
        for (QueryResult qResult : folderResults) {
            FSFolder fsFolder = new FSFolder();
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Folder folder = (Folder) session.getObject(session.createObjectId(objectId));
            fsFolder.setPath(folder.getPath());
            fsFolder.setName(folder.getName());
            fsFolder.setId(folder.getId());
            fsFolder.setTypeId(folder.getType().getDisplayName());
            fsFolder.setParentTypeId(folder.getType().getParentTypeId());
            files.add(fsFolder);
        }
    }

    /**
     * Collects complex full query for search in repository
     *
     * @param query - part of searching word for query
     * @return full query
     */
    public String getFullQuery(String query, ObjectType type) {
        return "SELECT " + "*" + " FROM " + type.getQueryName() + " WHERE cmis:name LIKE '%" + query + "%'";
    }

    /**
     * Find all cmis documents from searching in repository by query
     *
     * @param query - part of searching word for query
     * @param files - List for results
     * @return List files
     */
    public void parseFSFile(String query, List<FSObject> files) {
        ObjectType type = session.getTypeDefinition("cmis:document");
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String queryString = getFullQuery(query, type);
        ItemIterable<QueryResult> fileResult = session.query(queryString, false);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        for (QueryResult qResult : fileResult) {
            FSFile fsFile = new FSFile();
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Document doc = (Document) session.getObject(session.createObjectId(objectId));
            fsFile.setName(doc.getName());
            fsFile.setId(doc.getId());
            fsFile.setVersionable(((DocumentType) (doc.getType())).isVersionable());
            fsFile.setTypeId(doc.getType().getId());
            fsFile.setParentTypeId(doc.getType().getParentTypeId());
            fsFile.setCreatedBy(doc.getCreatedBy());
            fsFile.setCreationTime(doc.getCreationDate().getTime());
            fsFile.setLastModifiedBy(doc.getLastModifiedBy());
            fsFile.setLastModifiedTime(doc.getLastModificationDate().getTime());
            fsFile.setAbsolutePath(doc.getPaths().get(0));
            fsFile.setSize(String.valueOf(doc.getContentStreamLength()));
            files.add(fsFile);
        }
    }
    private String getQuery(Map<Integer, Object> query) {

        QueryStatement qs;
        String plusQuery = "SELECT * FROM " + query.get(0);
        int counter = 0;

        for (int i = 1; i < 6; ++i) {
            if (!query.get(i).equals("")) {
                if (counter == 0) {
                    plusQuery += " WHERE ";
                }
                qs = session.createQueryStatement(searchAdvancedParametrs.get(i));
                logger.log(Level.INFO, "===prop____# " + i + "=="+query.get(0)+"===="+query.get(i)+"===");
                qs.setString(1,(String) query.get(i));
                logger.log(Level.INFO, "===prop____# " + i + "=="+qs.toQueryString()+"=="+query.get(i)+"===");
                if (counter > 0) {
                    plusQuery += " AND ";
                }
                plusQuery += qs.toQueryString();
                ++counter;
            }
        }

        for (int i = 6; i < 8; ++i) {
            if (query.get(i) != null) {
                if (counter == 0) {
                    plusQuery += " WHERE ";
                }
                qs = session.createQueryStatement(searchAdvancedParametrs.get(i));
                qs.setDateTime(1, (Date) query.get(i));

                logger.log(Level.INFO, "===prop____# " + i + "=="+qs.toQueryString()+"=="+query.get(i));
                if (counter > 0) {
                    plusQuery += " AND ";
                }
                plusQuery += qs.toQueryString();
                ++counter;
            }
        }


        for (int i = 8; i < 10; ++i) {
            if (query.get(i) != "") {
                if (counter == 0) {
                    plusQuery += " WHERE ";
                }
                qs = session.createQueryStatement(searchAdvancedParametrs.get(i));
                qs.setString(1, (String) query.get(i));
                logger.log(Level.INFO, "===prop____# " + i + "=="+qs.toQueryString()+"==");
                if (counter > 0) {
                    plusQuery += " AND ";
                }
                plusQuery += searchAdvancedParametrs.get(i) + query.get(i) + "\" ";
                ++counter;
            }
        }

        logger.log(Level.INFO, "______________________" + plusQuery + "__________________");

        return plusQuery;
    }

    private void parseFSFile(List<FSObject> files, String objectIdQueryName, ItemIterable<QueryResult> fileResult) {
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
            fsFile.setSize(String.valueOf(doc.getContentStreamLength()));
            files.add(fsFile);
        }
    }

    private void parseFSFolder(List<FSObject> files, String objectIdQueryName, ItemIterable<QueryResult> folderResults) {
        for (QueryResult qResult : folderResults) {
            FSFolder fsFolder = new FSFolder();
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Folder folder = (Folder) session.getObject(session.createObjectId(objectId));
            fsFolder.setPath(folder.getPath());
            fsFolder.setName(folder.getName());
            fsFolder.setId(folder.getId());
            fsFolder.setTypeId(folder.getType().getDisplayName());
            fsFolder.setParentTypeId(folder.getType().getParentTypeId());
            files.add(fsFolder);
        }
    }


}
