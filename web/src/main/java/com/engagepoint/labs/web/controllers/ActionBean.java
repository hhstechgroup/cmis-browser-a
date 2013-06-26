package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Rostik
 * Date: 25.06.13
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean(name = "action")
@ApplicationScoped
public class ActionBean implements Serializable {

    private String name;
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private final CMISService service = new CMISServiceImpl();

    public FSFolder createFolder(FSFolder parent){
        logger.log(Level.INFO, "CREATE FOLDER, name: "+name+" and parent: "+parent.getPath());
        return service.createFolder(parent, name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
