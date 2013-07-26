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

public class FSFolderDaoImpl implements FSFolderDao {

    private Session session;
    private FSFileDao fsFileDao;
    private Map<Integer, String> searchAdvancedParametrs;

    public FSFolderDaoImpl() {
        fsFileDao = new FSFileDaoImpl();

        searchAdvancedParametrs = new HashMap<Integer, String>();
        searchAdvancedParametrs.put(0, "SELECT * FROM ? WHERE ");
        searchAdvancedParametrs.put(1, " cmis:name LIKE ? ");
        searchAdvancedParametrs.put(2, " cmis:objectTypeId LIKE ? ");
        searchAdvancedParametrs.put(3, " cmis:contentStreamMimeType LIKE ? ");
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
            folder = (FSFolder) fsFileDao.convertCmisObjectToFSObject(newFolder, parent);
        } catch (CmisNameConstraintViolationException ex) {
            throw new FolderAlreadyExistException(ex.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
        return folder;
    }


    @Override
    public List<FSObject> getChildren(FSFolder parent) throws BaseException /*throws BaseException*/ {
        List<FSObject> children = new ArrayList<FSObject>();
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        ItemIterable<CmisObject> cmisChildren = cmisParent.getChildren();
        try {
            for (CmisObject o : cmisChildren) {
                FSObject fsObject = fsFileDao.convertCmisObjectToFSObject(o, parent);
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
        FSFolder root = (FSFolder) fsFileDao.convertCmisObjectToFSObject(cmisRoot, null);
        return root;
    }

    @Override
    public void move(FSFile target) throws BrowserRuntimeException {
        Document doc = (Document) session.getObject(target.getId());
        Folder cmisFolder2 = (Folder) session.getObjectByPath("/");
        Folder parent = doc.getParents().get(0);
        ObjectId objectId = new ObjectIdImpl(cmisFolder2.getId());
        ObjectId parObjectId = new ObjectIdImpl(parent.getId());
        try {
            doc.move(parObjectId, objectId);
        } catch (CmisRuntimeException e) {
            throw new BrowserRuntimeException("Cant move file!");
        }
    }


    @Override
    public Map<String, Object> getPageForLazy(FSFolder parent, int first, int pageSize) throws BaseException {
        String notRootFolder = parent.getPath().equals("/") ? "" : parent.getPath();
        List<FSObject> children = new ArrayList<FSObject>();
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());

        Map<String, Object> page = new HashMap<String, Object>();
        ItemIterable<CmisObject> cmisChildrenForDatasize = cmisParent.getChildren();
        int dataSize = (int) cmisChildrenForDatasize.getTotalNumItems();
        page.put("datasize", dataSize);
        int pageSizeTroubleproof = pageSize;
        int firstTroubleproof = first;

        if (first < dataSize) {
            if (dataSize > pageSize) {
                if ((first + pageSize) > dataSize) {
                    pageSizeTroubleproof = dataSize - first;
                }
            } else {
                pageSizeTroubleproof = dataSize - first;
            }
        } else {
            if (dataSize % pageSize != 0) {
                pageSizeTroubleproof = dataSize % pageSize;
                firstTroubleproof = dataSize - (dataSize % pageSize + 1);
            } else {
                pageSizeTroubleproof = dataSize - pageSize;
                firstTroubleproof = pageSize;
                if (dataSize == 0) {
                    pageSizeTroubleproof = pageSize;
                    firstTroubleproof = 0;
                }
            }
        }


        OperationContext operationContext = session.createOperationContext();
        operationContext.setMaxItemsPerPage(pageSizeTroubleproof);
        ItemIterable<CmisObject> childrenCmis = cmisParent.getChildren(operationContext);
        ItemIterable<CmisObject> cmisChildren = childrenCmis.skipTo(firstTroubleproof).getPage();
        try {
            for (CmisObject o : cmisChildren) {
                FSObject fsObject = fsFileDao.convertCmisObjectToFSObject(o, parent);
                children.add(fsObject);
            }
        } catch (CmisObjectNotFoundException e) {
            throw new ConnectionException(e.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
        page.put("page", children);
        return page;
    }

    @Override
    public Map<String, Object> find(int first, int pageSize, String query, FSObject parent) {
        List<FSObject> files = new ArrayList<FSObject>();
        parseFSFile(query, files, parent);
        parseFSFolder(query, files, parent);

        int dataSize = files.size();
        Map<String, Object> page = new HashMap<String, Object>();

        page.put("datasize", dataSize);
        if (dataSize == 0) {
            page.put("page", files);
            return page;
        }
        if (first < dataSize) {
            if (dataSize > pageSize) {
                if ((first + pageSize) > dataSize) {
                    page.put("page", files.subList(first, dataSize));
                    return page;
                } else {
                    page.put("page", files.subList(first, first + pageSize));
                    return page;
                }
            } else {
                page.put("page", files.subList(first, dataSize));
                return page;
            }
        } else {
            if (dataSize % pageSize != 0) {
                page.put("page", files.subList(dataSize - dataSize % pageSize, dataSize));
                return page;
            } else {
                page.put("page", files.subList(dataSize - pageSize, dataSize));
                return page;
            }
        }
    }

    @Override
    public Map<String, Object> find(int first, int pageSize, Map<Integer, Object> query, FSObject parent) {
        List<FSObject> files = new LinkedList<FSObject>();
        String myType = (String) query.get(0);
        ObjectType type = session.getTypeDefinition(myType);
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        String queryString = getQuery(query, parent);
        ItemIterable<QueryResult> Results = session.query(queryString, false);
        if (myType.equals("cmis:document")) {
            parseFSFile(files, objectIdQueryName, Results);
        } else {
            parseFSFolder(files, objectIdQueryName, Results);
        }

        int dataSize = files.size();
        Map<String, Object> page = new HashMap<String, Object>();

        page.put("datasize", dataSize);
        if (dataSize == 0) {
            page.put("page", files);
            return page;
        }
        if (first < dataSize) {
            if (dataSize > pageSize) {
                if ((first + pageSize) > dataSize) {
                    page.put("page", files.subList(first, dataSize));
                    return page;
                } else {
                    page.put("page", files.subList(first, first + pageSize));
                    return page;
                }
            } else {
                page.put("page", files.subList(first, dataSize));
                return page;
            }
        } else {
            if (dataSize % pageSize != 0) {
                page.put("page", files.subList(dataSize - dataSize % pageSize, dataSize));
                return page;
            } else {
                page.put("page", files.subList(dataSize - pageSize, dataSize));
                return page;
            }
        }
    }

    @Override
    public boolean hasChildFolder(FSFolder folder) throws BaseException {
        List<FSObject> children;
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
            folder = (FSFolder) fsFileDao.convertCmisObjectToFSObject(cmisFolder, folder.getParent());
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
        List<FSObject> children;
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
    public void move(FSFolder source, FSFolder target) throws BrowserRuntimeException {
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
    }

    @Override
    public void copyFolder(FSFolder folder, String name, String targetId) throws FolderAlreadyExistException {
        Folder cmisFolderSource = (Folder) session.getObject(folder.getId());
        Folder cmisFolderTarget = (Folder) session.getObject(targetId);
        ItemIterable<CmisObject> cmisFolderSourceChildren = cmisFolderSource.getChildren();
        Map<String, String> newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, name);

        Folder copySourceFolder;
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

    public void parseFSFolder(String query, List<FSObject> files, FSObject parent) {
        ObjectType type = session.getTypeDefinition("cmis:folder");
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        String queryString = getFullQuery(query, type, parent);
        ItemIterable<QueryResult> folderResults = session.query(queryString, false);
        for (QueryResult qResult : folderResults) {
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Folder folder = (Folder) session.getObject(session.createObjectId(objectId));
            FSFolder fsFolder = (FSFolder) fsFileDao.convertCmisObjectToFSObject(folder, null);
            files.add(fsFolder);
        }
    }

    /**
     * Collects complex full query for search in repository
     *
     * @param query - part of searching word for query
     * @return full query
     */
    public String getFullQuery(String query, ObjectType type, FSObject parent) {
        return "SELECT " + "*" + " FROM " + type.getQueryName() + " WHERE IN_TREE('" + parent.getId() + "') AND cmis:name LIKE '%" + query + "%'";
    }

    /**
     * Find all cmis documents from searching in repository by query
     *
     * @param query - part of searching word for query
     * @param files - List for results
     * @return List files
     */
    public void parseFSFile(String query, List<FSObject> files, FSObject parent) {
        ObjectType type = session.getTypeDefinition("cmis:document");
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String queryString = getFullQuery(query, type, parent);
        ItemIterable<QueryResult> fileResult = session.query(queryString, false);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        for (QueryResult qResult : fileResult) {
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Document doc = (Document) session.getObject(session.createObjectId(objectId));
            FSFile fsFile = (FSFile) fsFileDao.convertCmisObjectToFSObject(doc, null);
            files.add(fsFile);
        }
    }

    private String getQuery(Map<Integer, Object> query, FSObject parent) {

        QueryStatement qs;
        String plusQuery = "SELECT * FROM " + query.get(0);
        int counter = 0;

        for (int i = 1; i < 6; ++i) {
            if (query.get(i) != "") {
                if (counter == 0) {
                    plusQuery += " WHERE ";
                }
                qs = session.createQueryStatement(searchAdvancedParametrs.get(i));
                qs.setString(1, (String) query.get(i));
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

                if (counter > 0) {
                    plusQuery += " AND ";
                }
                plusQuery += qs.toQueryString();
                ++counter;
            }
        }

        for (int i = 8; i < 10; ++i) {
            if (query.get(i) != "" && query.get(i) != null) {
                if (counter == 0) {
                    plusQuery += " WHERE ";
                }
                if (counter > 0) {
                    plusQuery += " AND ";
                }
                plusQuery += searchAdvancedParametrs.get(i) + query.get(i) + "\" ";
                ++counter;
            }
        }
        if (counter == 0) {
            plusQuery += " WHERE ";
        } else {
            plusQuery += " AND ";
        }
        qs = session.createQueryStatement(" IN_TREE(?) ");
        qs.setString(1, parent.getId());
        plusQuery += qs.toQueryString();
        return plusQuery;
    }

    private void parseFSFile(List<FSObject> files, String objectIdQueryName, ItemIterable<QueryResult> fileResult) {
        for (QueryResult qResult : fileResult) {
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Document doc = (Document) session.getObject(session.createObjectId(objectId));
            FSFile fsFile = (FSFile) fsFileDao.convertCmisObjectToFSObject(doc, null);
            files.add(fsFile);
        }
    }

    private void parseFSFolder(List<FSObject> files, String objectIdQueryName, ItemIterable<QueryResult> folderResults) {
        for (QueryResult qResult : folderResults) {
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
            Folder folder = (Folder) session.getObject(session.createObjectId(objectId));
            FSFolder fsFolder = (FSFolder) fsFileDao.convertCmisObjectToFSObject(folder, null);
            files.add(fsFolder);
        }
    }


}
