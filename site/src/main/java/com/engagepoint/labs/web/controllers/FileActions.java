package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.MimeTypes;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: r.reznichenko
 * Date: 7/9/13
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean(name = "fileActions")
@SessionScoped
public class FileActions implements Serializable {

    private CMISService cmisService;
    private static Logger logger;
    private boolean selectedIsFile;
    private String selectedName;
    private boolean versionable;
    private UploadedFile file;

    public FileActions() {
        cmisService = CMISServiceImpl.getService();
        logger = Logger.getLogger(FileActions.class.getName());
        selectedIsFile = false;
    }

    public StreamedContent download(FSFile file) throws IllegalArgumentException {
        logger.log(Level.INFO, "download: "+file.getName());
        String fileName = file.getName();
        if (file.isVersionable()) {
            logger.log(Level.INFO, "file is versinable: "+file.getVersionLabel());
            fileName += (file.getVersionLabel() == null) ? "" : file.getVersionLabel();
        }
        logger.log(Level.INFO, "id: "+file.getId());
        InputStream inputStream = cmisService.getInputStream(file);
        String extension = MimeTypes.getExtension(file.getMimetype());
        return new DefaultStreamedContent(inputStream, file.getMimetype(), fileName+extension);
    }

    public boolean isSelectedIsFile() {
        return selectedIsFile;
    }

    public void setSelectedIsFile(boolean selectedIsFile) {
        this.selectedIsFile = selectedIsFile;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public String getSelectedName() {
        return selectedName;
    }

    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }

    public boolean isVersionable() {
        return versionable;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
    }
}
