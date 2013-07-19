package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.MimeTypes;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.ConnectionException;
import com.engagepoint.labs.core.models.exceptions.FileNotFoundException;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
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
        try {
            cmisService = CMISServiceImpl.getService();
        } catch (ConnectionException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "Try later"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        logger = Logger.getLogger(FileActions.class.getName());
        selectedIsFile = false;
    }

    public StreamedContent download(FSFile file) throws IllegalArgumentException, FileNotFoundException {
        logger.log(Level.INFO, "download: " + file.getName());
        String fileName = file.getName();
        if (file.isVersionable()) {
            logger.log(Level.INFO, "file is versinable: " + file.getVersionLabel());
            fileName += (file.getVersionLabel() == null) ? "" : file.getVersionLabel();
        }
        logger.log(Level.INFO, "id: " + file.getId());
        InputStream inputStream = null;

        try {
            inputStream = cmisService.getInputStream(file);
            logger.log(Level.INFO, "inputStream - null? - " + (inputStream == null));

        } catch (FileNotFoundException e) {
            logger.log(Level.INFO, "Is inputStream - null? - " + (inputStream == null));
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "Maybe, file was deleted!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        logger.log(Level.INFO, "ggggggggggggggggggggggggggg");
        String extension = MimeTypes.getExtension(file.getMimetype());
        logger.log(Level.INFO, "wwwwwwwwwwwwwwwwwwwwwwwwwwww");
        DefaultStreamedContent defaultStreamedContent = null;
        try {
            if (inputStream != null)
            defaultStreamedContent = new DefaultStreamedContent(inputStream, file.getMimetype(), fileName + extension);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "Maybe, file was deleted!"));
        }

        return defaultStreamedContent;
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
