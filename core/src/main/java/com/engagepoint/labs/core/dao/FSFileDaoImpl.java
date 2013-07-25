package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.FileAlreadyExistException;
import com.engagepoint.labs.core.models.exceptions.FileNotFoundException;
import com.engagepoint.labs.core.models.exceptions.UnauthorizedException;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;

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
    public FSFile create(FSFolder parent, String fileName, byte[] content, String mimeType) throws BaseException {
        FSFile file;
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(content);

            ContentStream contentStream = session.getObjectFactory().createContentStream(fileName, content.length, mimeType, input);

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            properties.put(PropertyIds.NAME, fileName);

            Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());
            Document doc = cmisParent.createDocument(properties, contentStream, VersioningState.NONE);

            file = (FSFile) convertCmisObjectToFSObject(doc, parent);
        } catch (CmisNameConstraintViolationException ex) {
            throw new FileAlreadyExistException(ex.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
        return file;
    }


    @Override
    public FSFile edit(FSFile file, byte[] content, String mimeType) throws BaseException {
        Document cmisFile = (Document) session.getObject(file.getId());
        Map<String, String> properties = null;
        if (!file.getName().equals(cmisFile.getName())) {
            properties = new HashMap<String, String>();
            properties.put(PropertyIds.NAME, file.getName());
        }
        InputStream input;
        ContentStream contentStream;
        try {
            input = new ByteArrayInputStream(content);
            contentStream = session.getObjectFactory().createContentStream(file.getName(),
                    content.length, mimeType, input);
        } catch (NullPointerException ex) {
            contentStream = cmisFile.getContentStream();
        }

        if (((DocumentType) (cmisFile.getType())).isVersionable()) {
            try {
                Document pwc = (Document) session.getObject(cmisFile.checkOut());
                // Check in the pwc
                try {
                    pwc.checkIn(false, properties, contentStream, "minor version");
                    try {
                        pwc.updateProperties(properties, true);
                    } catch (CmisNameConstraintViolationException e) {
                        throw new FileAlreadyExistException("File with such name already exist!");
                    }
                } catch (CmisBaseException e) {
                    System.out.println("checkin failed, trying to cancel the checkout");
                    pwc.cancelCheckOut();
                }
            } catch (CmisUnauthorizedException e) {
                throw new UnauthorizedException("Authorization Required");
            } catch (CmisBaseException e) {
                throw new BaseException(e.getMessage());
            }
        } else {
            cmisFile.setContentStream(contentStream, true, true);
            if (properties != null) {
                try {
                    cmisFile.updateProperties(properties, true);
                } catch (CmisNameConstraintViolationException e) {
                    throw new FileAlreadyExistException("File with such name already exist!");
                } catch (CmisBaseException e) {
                    throw new BaseException(e.getMessage());
                }
            }
        }
        file = (FSFile) convertCmisObjectToFSObject(cmisFile, file.getParent());
        return file;
    }

    @Override
    public boolean delete(FSFile file) {
        Document doc = (Document) session.getObjectByPath(file.getAbsolutePath());
        doc.delete(true);
        return true;
    }

    @Override
    public InputStream getInputStream(FSFile file) throws BaseException {
        Document cmisFile;
        ContentStream contentStream;
        try {
            cmisFile = (Document) session.getObject(file.getId());
            contentStream = cmisFile.getContentStream();
        } catch (CmisObjectNotFoundException e) {
            throw new FileNotFoundException("File not found in this repository!");
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }

        return contentStream.getStream();
    }

    @Override
    public boolean copy(String id, String newName, String targetId) throws BaseException {
        String tempName = "TempCopyName";
        Document document = (Document) session.getObject(id);
        String defaultName = document.getName();

        ObjectId targetObjId = new ObjectIdImpl(targetId);
        Map<String, String> newDocumentProperties = new HashMap<String, String>();
        newDocumentProperties.put(PropertyIds.NAME, tempName);
        document.updateProperties(newDocumentProperties);
        Document copiedDocument = null;
        try {
            copiedDocument = document.copy(targetObjId);
            newDocumentProperties.put(PropertyIds.NAME, newName);
            try {
                copiedDocument.updateProperties(newDocumentProperties);
            } catch (CmisNameConstraintViolationException e) {
                copiedDocument.delete();
                throw new FileAlreadyExistException("File with such name already exist!");
            } catch (CmisBaseException e) {
                throw new BaseException(e.getMessage());
            }
        } catch (CmisNameConstraintViolationException e) {
            throw new FileAlreadyExistException("You cant copy this document in his parent folder");
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        } finally {
            newDocumentProperties.put(PropertyIds.NAME, defaultName);
            document.updateProperties(newDocumentProperties);
        }
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
                FSFile versionFile = (FSFile) convertCmisObjectToFSObject(version, file.getParent());
                versionFile.setVersionLabel(version.getVersionLabel());
                fileVersions.add(versionFile);
            }
            file.setAllVersions(fileVersions);
            return file;
        }
        return null;
    }

    public FSObject convertCmisObjectToFSObject(CmisObject cmisObject, FSFolder parent) {
        FSObject fsObject;
        if (cmisObject instanceof Folder) {
            fsObject = new FSFolder();
            fsObject.setPath(((Folder) cmisObject).getPath());
        } else {
            fsObject = new FSFile();
            fsObject.setMimetype(((Document) cmisObject).getContentStreamMimeType());
            if (parent != null) {
                fsObject.setPath(parent.getPath());
            }
            ((FSFile) fsObject).setVersionable(((DocumentType) (cmisObject.getType())).isVersionable());
            fsObject.setSize(String.valueOf(((Document) cmisObject).getContentStreamLength() / 1024));
            ((FSFile) fsObject).setAbsolutePath(((Document) cmisObject).getPaths().get(0));
        }
        fsObject.setParentTypeId(cmisObject.getType().getParentTypeId());
        fsObject.setCreatedBy(cmisObject.getCreatedBy());
        fsObject.setCreationTime(cmisObject.getCreationDate().getTime());
        fsObject.setLastModifiedBy(cmisObject.getLastModifiedBy());
        fsObject.setLastModifiedTime(cmisObject.getLastModificationDate().getTime());
        fsObject.setTypeId(cmisObject.getType().getId());
        fsObject.setName(cmisObject.getName());
        fsObject.setId(cmisObject.getId());
        if (parent != null) {
            fsObject.setParent(parent);
        }
        return fsObject;
    }

}
