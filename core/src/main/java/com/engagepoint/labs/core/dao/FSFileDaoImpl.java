package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
    public FSFile edit(FSFile file, byte[] content, String mimeType) {
        Document cmisFile = (Document) session.getObject(file.getId());
        Map<String, String> properties = null;
        logger.log(Level.INFO, "file name: " + file.getName() + " cmisfile name: " + cmisFile.getName());
        if (!file.getName().equals(cmisFile.getName())) {
            logger.log(Level.INFO, "renaming-- old name: " + cmisFile.getName() + " new name: " + file.getName());
            properties = new HashMap<String, String>();
            properties.put(PropertyIds.NAME, file.getName());
        }
        if (((DocumentType) (cmisFile.getType())).isVersionable()) {
            logger.log(Level.INFO, "isVersionable");
            Document pwc = (Document) session.getObject(cmisFile.checkOut());
            if (content != null && mimeType != null) {
                InputStream input = new ByteArrayInputStream(content);
                ContentStream contentStream = session.getObjectFactory().createContentStream(file.getName(),
                        content.length, mimeType, input);

                // Check in the pwc
                try {
                    logger.log(Level.INFO, "properties null ?" + (properties == null));
                    pwc.checkIn(true, properties, contentStream, "minor version");
                } catch (CmisBaseException e) {
                    System.out.println("checkin failed, trying to cancel the checkout");
                    pwc.cancelCheckOut();
                }
            }
        } else {
            if (content != null && mimeType != null) {
                InputStream input = new ByteArrayInputStream(content);
                ContentStream contentStream = session.getObjectFactory().createContentStream(file.getName(),
                        content.length, mimeType, input);
                cmisFile.setContentStream(contentStream, true, true);
            }
            if (properties != null) {
                cmisFile.updateProperties(properties, true);
            }
            file.setMimetype(mimeType);
            file.setLastModifiedBy(cmisFile.getLastModifiedBy());
            file.setLastModifiedTime(cmisFile.getLastModificationDate().getTime());
            file.setTypeId(cmisFile.getBaseType().getDisplayName());
            file.setSize(String.valueOf(cmisFile.getContentStreamLength() / 1024));
            file.setParentTypeId(cmisFile.getType().getParentTypeId());
            file.setName(cmisFile.getName());
            file.setAbsolutePath(cmisFile.getPaths().get(0));
        }
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
