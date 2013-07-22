package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 4:02 PM
 */
public class FSFileDaoImpl implements FSFileDao {

    private Session session;
    private static Logger logger = Logger.getLogger(FSFileDaoImpl.class.getName());

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
    public boolean copy(String id, String newName, String targetId) {
        String tempName = "TempCopyName";
        Document document = (Document) session.getObject(id);
        Folder folder = (Folder) session.getObject(targetId);
        String defaultName = document.getName();

        ObjectId targetObjId = new ObjectIdImpl(targetId);
        Map<String, String> newDocumentProperties = new HashMap<String, String>();
        newDocumentProperties.put(PropertyIds.NAME, tempName);
        document.updateProperties(newDocumentProperties);
        Document copiedDocument = null;
        copiedDocument = document.copy(targetObjId);
        newDocumentProperties.put(PropertyIds.NAME, newName);
        copiedDocument.updateProperties(newDocumentProperties);
        copiedDocument.delete();
        newDocumentProperties.put(PropertyIds.NAME, defaultName);
        document.updateProperties(newDocumentProperties);
        System.out.println("Documents ID which we are copy is " + document.getId());
        System.out.println("Copied document ID is " + copiedDocument.getId());
        System.out.println("Document is copied successfully");
        return true;
    }

    @Override
    public FSFile getHistory(FSFile file) {
        Document cmisFile = (Document) session.getObject(file.getId());
        if (((DocumentType) (cmisFile.getType())).isVersionable()) {
            List<Document> versions = cmisFile.getAllVersions();
            List<FSFile> fileVersions = new ArrayList<FSFile>(versions.size());
            for (Document version : versions) {
                FSFile versionFile = new FSFile();
                versionFile.setName(version.getName());
                versionFile.setVersionLabel(version.getVersionLabel());
                versionFile.setLastModifiedBy(version.getLastModifiedBy());
                versionFile.setLastModifiedTime(version.getLastModificationDate().getTime());
                versionFile.setSize(String.valueOf(version.getContentStreamLength() / 1024));
                versionFile.setId(version.getId());
                versionFile.setVersionable(true);
                versionFile.setMimetype(version.getContentStreamMimeType());
                versionFile.setTypeId(version.getBaseType().getDisplayName());
                versionFile.setParentTypeId(version.getType().getParentTypeId());
                versionFile.setAbsolutePath(version.getPaths().get(0));
                fileVersions.add(versionFile);
            }
            file.setAllVersions(fileVersions);
            return file;
        }
        return null;
    }

}
