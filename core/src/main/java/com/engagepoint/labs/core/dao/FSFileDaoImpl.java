package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.QueryStatementImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 4:02 PM
 */
public class FSFileDaoImpl implements FSFileDao {

    private Session session;

    private static Logger logger = Logger.getLogger(FSFileDao.class.getName());

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public FSFile create(FSFolder parent, String fileName, byte[] content, String mimeType) {
        String notRootFolder = parent.getPath().equals("/") ? "" : parent.getPath();

        ByteArrayInputStream input = new ByteArrayInputStream(content);

        ContentStream contentStream = session.getObjectFactory().createContentStream(fileName, content.length, mimeType, input);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, fileName);

        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
        Document doc = cmisParent.createDocument(properties, contentStream, VersioningState.NONE);

        FSFile file = new FSFile();

        file.setMimetype(mimeType);
        file.setPath(notRootFolder);
        file.setAbsolutePath(notRootFolder + "/" + fileName);
        file.setName(doc.getName());
        file.setParent(parent);
        file.setId(doc.getId());
        file.setTypeId(doc.getType().getId());
        file.setParentTypeId(doc.getType().getParentTypeId());
        file.setCreatedBy(doc.getCreatedBy());
        file.setCreationTime(doc.getCreationDate().getTime());
        file.setLastModifiedBy(doc.getLastModifiedBy());
        file.setLastModifiedTime(doc.getLastModificationDate().getTime());
        return file;
    }

    @Override
    public FSFile rename(FSFile file, String newName) {
        Document cmisFile = (Document) session.getObjectByPath(file.getAbsolutePath());
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PropertyIds.NAME, newName);
        cmisFile.updateProperties(properties, true);
        file.setName(cmisFile.getName());
        file.setAbsolutePath(cmisFile.getPaths().get(0));
        file.setLastModifiedBy(cmisFile.getLastModifiedBy());
        file.setLastModifiedTime(cmisFile.getLastModificationDate().getTime());
        file.setParentTypeId(cmisFile.getType().getParentTypeId());
        return file;
    }

    @Override
    public FSFile edit(FSFile file, byte[] content, String mimeType) {
        Document cmisFile = (Document) session.getObject(file.getId());
        if (content == null) {
            content = new byte[0];
        }
        InputStream input = new ByteArrayInputStream(content);
        ContentStream contentStream = session.getObjectFactory().createContentStream(file.getName(),
                content.length, mimeType, input);
        cmisFile.setContentStream(contentStream, true, true);
        file.setMimetype(mimeType);
        file.setLastModifiedBy(cmisFile.getLastModifiedBy());
        file.setLastModifiedTime(cmisFile.getLastModificationDate().getTime());
        file.setTypeId(cmisFile.getBaseType().getDisplayName());
        file.setSize(String.valueOf(contentStream.getLength() / 1024));
        file.setParentTypeId(cmisFile.getType().getParentTypeId());
        return file;
    }

    @Override
    public boolean delete(FSFile file) {
        Document doc = (Document) session.getObjectByPath(file.getAbsolutePath());
        doc.delete(true);
        return true;
    }

    @Override
    public InputStream getInputStream(FSFile file) {
        Document cmisFile = (Document) session.getObject(file.getId());
        ContentStream contentStream = cmisFile.getContentStream();
        return contentStream.getStream();
    }

    @Override
    public void copy(String id, String targetId) {
        Document doc = (Document) session.getObject(id);
        ObjectId targetObjId = new ObjectIdImpl(targetId);
        doc.copy(targetObjId);
    }

    @Override
    public List<FSObject> find(String query) {
        List<FSObject> files = new LinkedList<FSObject>();
        String myType = "cmis:document";
        ObjectType type = session.getTypeDefinition(myType);
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        String queryString = getQuery(query, type);
        ItemIterable<QueryResult> fileResult = session.query(queryString, false);
        parseFSFile(files, objectIdQueryName, fileResult);
        myType = "cmis:folder";
        type = session.getTypeDefinition(myType);
        objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        objectIdQueryName = objectIdPropDef.getQueryName();
        queryString = getQuery(query, type);
        ItemIterable<QueryResult> folderResults = session.query(queryString, false);
        parseFSFolder(files, objectIdQueryName, folderResults);
        return files;
    }

    @Override
    public List<FSObject> find(List<Object> query) {
        List<FSObject> files = new LinkedList<FSObject>();
        String myType = "cmis:document";
        ObjectType type = session.getTypeDefinition(myType);
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        String queryString = getQuery(query, type);
        ItemIterable<QueryResult> fileResult = session.query(queryString, false);
        parseFSFile(files, objectIdQueryName, fileResult);
        myType = "cmis:folder";
        type = session.getTypeDefinition(myType);
        objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        objectIdQueryName = objectIdPropDef.getQueryName();
//        logger.log(Level.INFO, "============DAOOOOOOO!======" );
        queryString = getQuery(query, type);
//        logger.log(Level.INFO, "============DAOOOOOOO!!======" );
        ItemIterable<QueryResult> folderResults = session.query(queryString, false);
//        logger.log(Level.INFO, "============DAOOOOOOO!!!======" );
        parseFSFolder(files, objectIdQueryName, folderResults);
//        logger.log(Level.INFO, "============DAOOOOOOO!!!!======" );
        return files;
    }

    private void parseFSFolder(List<FSObject> files, String objectIdQueryName, ItemIterable<QueryResult> folderResults) {
//        logger.log(Level.INFO, "============PARSE!======" );
        for (QueryResult qResult : folderResults) {
//            logger.log(Level.INFO, "============PARSE!!======" );
            FSFolder fsFolder = new FSFolder();
            String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
//            logger.log(Level.INFO, "============PARSE!!!======" );
            Folder folder = (Folder) session.getObject(session.createObjectId(objectId));
//            logger.log(Level.INFO, "============PARSE!!!!======" );
            fsFolder.setPath(folder.getPath());
            fsFolder.setName(folder.getName());

            fsFolder.setId(folder.getId());
            fsFolder.setTypeId(folder.getType().getDisplayName());
            fsFolder.setParentTypeId(folder.getType().getParentTypeId());
            files.add(fsFolder);
//            logger.log(Level.INFO, "============PARSE!!!!======" );
        }
    }


    private String getQuery(String query, ObjectType type) {
        return "SELECT " + "*" + " FROM " + type.getQueryName() + " WHERE cmis:name LIKE '%" + query + "%'";
    }

    private String getQuery(List<Object> query, ObjectType type) {


//        searchQueryAdvanced.add(findName);
//        searchQueryAdvanced.add(cmisType);
//        searchQueryAdvanced.add(metaDataType);
//        searchQueryAdvanced.add(docType);
//        searchQueryAdvanced.add(calendarFrom);
//        searchQueryAdvanced.add(calendarTo);
//        searchQueryAdvanced.add(contentType);
//        searchQueryAdvanced.add(sizeFrom);
//        searchQueryAdvanced.add(sizeTo);
//        searchQueryAdvanced.add(snippet);

        String plusQuery = "SELECT * FROM " + type.getQueryName() + " WHERE ";
        int count = 0;
        for (Object subQuery : query) {
            if ((count == 0) && (subQuery != "")) {

                plusQuery += " cmis:name LIKE '%" + (String)subQuery + "%'";
                if(type.getQueryName() == "cmis:document")  {
                    plusQuery += " AND " ;
                }
//                logger.log(Level.INFO, "===Count " + queryFull + "====");
            }

//            if ((count == 1) && ((String)subQuery != "")) {
//                plusQuery += " AND cmis:objectTypeId LIKE '%" + subQuery + "%'";
//            }
//            if ((count == 2) && ((String)subQuery != "")) {
////                plusQuery += " AND cmis:objectTypeId LIKE '%" + subQuery +"%'";
//            }
//            if ((count == 3) && ((String) subQuery != "") && (type.getQueryName() == "cmis:document")) {
//                plusQuery += " AND cmis:contentStreamMimeType LIKE '%" + subQuery + "%'";
//            }
//            if ((count == 4) && ((Date)subQuery != null)) {
//               plusQuery += " AND cmis:lastModificationDate > TIMESTAMP '?'";
//            }
//            if ((count == 5) && ((Date)subQuery != null)) {
//                plusQuery += " AND cmis:lastModificationDate < TIMESTAMP '?'";
//            }
//            if ((count == 5) && ((String)subQuery != "")) {
//
//            }
            if ((count == 7) && ((String) subQuery != "") && (type.getQueryName() == "cmis:document")) {
                plusQuery += "  cmis:contentStreamLength > \"" + subQuery + "\"";
                logger.log(Level.INFO, "___________sizejsdhflgsdh___________" + plusQuery + "__________________");
            }
            if ((count == 8) && ((String) subQuery != "") && (type.getQueryName() == "cmis:document")) {
                plusQuery += " AND cmis:contentStreamLength < \"" + subQuery + "\"";
                logger.log(Level.INFO, "_________sizedebhshedbsgb_____________" + plusQuery + "__________________");

            }
            if ((count == 9) && ((String)subQuery != "") && (type.getQueryName() == "cmis:document")) {
                plusQuery += " AND CONTAINS('" + subQuery + "')";

            }
//            //SELECT * FROM cmis:document WHERE cmis:lastModificationDate < TIMESTAMP '2014-05-22T00:00:00.000+00:00'
//            // AND cmis:lastModificationDate > TIMESTAMP '2013-07-02T00:00:00.000+00:00'
            logger.log(Level.INFO, "__________|||||sizedebhshedbsgb|||||||_____________"+count +"_____"+subQuery +"_____"+ plusQuery+"____" +type.getQueryName()+ "__________________");
            ++count;
        }

        logger.log(Level.INFO, "______________________" + plusQuery + "__________________");



        QueryStatement qs = session.createQueryStatement("SEL?ECT ?, ? FROM ? WHERE ? > TIMESTAMP ? AND IN_FOLDER(?) OR ? IN (?)");

        qs.setProperty(1, "cmis:document", "cmis:name");
        qs.setProperty(2, "cmis:document", "cmis:objectId");
        qs.setType(3, "cmis:document");

        qs.setProperty(4, "cmis:document", "cmis:creationDate");
//        qs.setDateTime(5, (Date)query.get(4));


        qs.setProperty(7, "cmis:document", "cmis:createdBy");
        qs.setString(8, "bob", "tom", "lisa");

        String statement = qs.toQueryString();




//        queryFull = "SELECT " + "*" + " FROM " + type.getQueryName() + " WHERE" ;


//        logger.log(Level.INFO, "===Count " + queryFull + "====");


//        for (Object subQuery : query) {
//            if ((count == 0) && (subQuery != "")) {
//
//                queryFull += " cmis:name LIKE '%" + (String)subQuery + "%'";
//                logger.log(Level.INFO, "===Count " + queryFull + "====");
//            }
//
//            if ((count == 1) && ((String)subQuery != "")) {
////                queryFull += " AND cmis:objectTypeId LIKE '%" + subQuery + "%'";
//            }
//            if ((count == 2) && ((String)subQuery != "")) {
////                queryFull += " AND cmis:objectTypeId LIKE '%" + subQuery +"%'";
//            }
//            if ((count == 3) && ((String)subQuery != "")) {
//
//            }
//            if ((count == 4) && ((String)subQuery != "")) {
//
//            }
//            if ((count == 5) && ((String)subQuery != "")) {
//
//            }
//            //SELECT * FROM cmis:document WHERE cmis:lastModificationDate < TIMESTAMP '2014-05-22T00:00:00.000+00:00'
//            // AND cmis:lastModificationDate > TIMESTAMP '2013-07-02T00:00:00.000+00:00'
//            ++count;
//        }
//        if (queryFull == "SELECT " + "*" + " FROM " + type.getQueryName() + " WHERE ") {
//            return null;
//        } else {
//            return queryFull;
//        }
//        return "SELECT * FROM " + type.getQueryName() +" WHERE cmis:name LIKE '%" + query.get(0) + "%'";
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


}
