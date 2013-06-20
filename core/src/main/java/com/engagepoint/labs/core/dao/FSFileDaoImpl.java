package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 4:02 PM
 */
public class FSFileDaoImpl implements FSFileDao {

    private Session session;
    @Override
    public void setSession(Session session) {
        this.session = session;

    }

    @Override
    public FSFile create(FSFolder parent, String fileName, String content) {
        Folder cmisParent = (Folder) session.getObjectByPath(parent.getPath());

        String mimetype = "text/plain; charset=UTF-8";

        byte[] buf = new byte[0];
        try {
            buf = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            //TODO do something with exception
        }
        ByteArrayInputStream input = new ByteArrayInputStream(buf);

        ContentStream contentStream = session.getObjectFactory()
                .createContentStream(fileName, buf.length, mimetype, input);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, fileName);

        Document doc = cmisParent.createDocument(properties, contentStream, VersioningState.NONE);

        String notRootFolder = parent.getPath().equals("/") ? "" : parent.getPath();
        FSFile file = new FSFile();
        file.setId(doc.getId());
        file.setName(fileName);
        file.setPath(notRootFolder);
        file.setAbsolutePath(notRootFolder + "/" + fileName);
        file.setContent(content);
        file.setParent(parent);
        file.setType(doc.getType().getDisplayName());
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
        return file;
    }

    @Override
    public String getContent(FSFile file) {
        Document cmisFile = (Document) session.getObjectByPath(file.getAbsolutePath());
        ContentStream contentStream = cmisFile.getContentStream();
        String content = null;
        if (contentStream != null) {
            try {
                content = getContentAsString(contentStream);
            } catch (IOException e) {
                //TODO do something with exception
            }
        }
        file.setContent(content);
        return content;
    }

    @Override
    public boolean delete(FSFile file) {
        Document doc = (Document) session.getObjectByPath(file.getAbsolutePath());
        doc.delete(true);
        return true;
    }

    private String getContentAsString(ContentStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        Reader reader = new InputStreamReader(stream.getStream(), "UTF-8");

        try {
            final char[] buffer = new char[4 * 1024];
            int b;
            while (true) {
                b = reader.read(buffer, 0, buffer.length);
                if (b > 0) {
                    sb.append(buffer, 0, b);
                } else if (b == -1) {
                    break;
                }
            }
        } finally {
            reader.close();
        }

        return sb.toString();
    }

}
