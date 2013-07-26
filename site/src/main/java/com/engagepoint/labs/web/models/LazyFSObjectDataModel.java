package com.engagepoint.labs.web.models;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.FolderNotFoundException;
import com.engagepoint.labs.core.service.CMISService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: anton.riabov
 * Date: 7/16/13
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class
        LazyFSObjectDataModel extends LazyDataModel<FSObject> {

    private static Logger logger = Logger.getLogger(LazyFSObjectDataModel.class.getName());

    private FSFolder parent;
    private CMISService cmisService;
    private String searchQuery;
    private Map<Integer, Object> searchQueryAdvanced;
    private boolean ableSearchAdvanced;


    public LazyFSObjectDataModel(CMISService cmisService, FSFolder parent) {
        this.cmisService = cmisService;
        this.parent = parent;
        this.searchQuery = "";
        searchQueryAdvanced = new HashMap<Integer, Object>();
        boolean ableSearchAdvanced = false;

        logger.log(Level.INFO, "===============================LazyFSObjectDataModel(CMISService cmisService, FSFolder parent) ========");
    }

    @Override
    public FSObject getRowData(String rowKey) {
        logger.log(Level.INFO, "===============================FSObject getRowData(String rowKey) ========");
        return null;
    }

    @Override
    public Object getRowKey(FSObject fsObject) {
        logger.log(Level.INFO, "===============================Object getRowKey(FSObject fsObject) ========");
        return fsObject.getName();
    }

    @Override
    public List<FSObject> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, String> filters) {
        logger.log(Level.INFO, "===============================List<FSObject> load fake ========");
        return null;
    }

    @Override
    public List<FSObject> load(int first, int pageSize, String sortField, org.primefaces.model.SortOrder sortOrder, Map<String, String> filters) {
        List<FSObject> data = new ArrayList<FSObject>();
        Map<String, Object> page;

        if (!ableSearchAdvanced) {
            if (searchQuery.equals("")) {
                try {
                    page = cmisService.getPageForLazy2(parent, first, pageSize);
                    this.setRowCount((Integer)page.get("datasize"));
                    return (List<FSObject>)page.get("page");
                } catch (BaseException e) {
                    return null;
                }

            } else {
                page = cmisService.getPageForLazySearchQuery2(first, pageSize, searchQuery);
                this.setRowCount((Integer)page.get("datasize"));
                return (List<FSObject>)page.get("page");

            }
        }
        // for advaced search
        else {
            page = cmisService.getPageForLazySearchQuery2(first, pageSize, searchQueryAdvanced);
            this.setRowCount((Integer)page.get("datasize"));
            return (List<FSObject>)page.get("page");

        }
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setSearchQueryAdvanced(Map<Integer, Object> searchQueryAdvanced) {
        logger.log(Level.INFO, "============SET ===searchQueryAdvanced===========");

        this.searchQueryAdvanced = searchQueryAdvanced;
        logger.log(Level.INFO, "============SET ===searchQueryAdvanced====" + searchQueryAdvanced + "=======");
    }

    public boolean isAbleSearchAdvanced() {
        return ableSearchAdvanced;
    }

    public void setAbleSearchAdvanced(boolean ableSearchAdvanced) {
        this.ableSearchAdvanced = ableSearchAdvanced;
    }

}