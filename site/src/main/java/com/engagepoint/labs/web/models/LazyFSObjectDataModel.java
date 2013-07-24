package com.engagepoint.labs.web.models;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.service.CMISService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

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

        if (!ableSearchAdvanced) {
            if (searchQuery.equals("")) {
                int dataSize = cmisService.getMaxNumberOfRows(parent);
                this.setRowCount(dataSize);
                logger.log(Level.INFO, "============DATASIZE==========" + dataSize + "========");

                if (dataSize > pageSize) {
                    try {
                        data = cmisService.getPageForLazy(parent, first, pageSize);
                    } catch (BaseException e) {
                    }
                    return data;
                } else {
                    try {
                        data = cmisService.getPageForLazy(parent, 0, pageSize);
                    } catch (BaseException e) {
                    }
                    return data;
                }
            } else {
                int dataSize = cmisService.getMaxNumberOfRowsByQuery(searchQuery);
                this.setRowCount(dataSize);
                logger.log(Level.INFO, "============DATASIZE_Query==========" + dataSize + "========");

                if (dataSize > pageSize) {
                    if ((first + pageSize) > dataSize) {
                        logger.log(Level.INFO, "============if((first + pageSize) > dataSize)==========" + dataSize + "========");
                        data = cmisService.getPageForLazySearchQuery(first, dataSize - first, searchQuery);
                        return data;
                    } else {
                        data = cmisService.getPageForLazySearchQuery(first, pageSize, searchQuery);
                        return data;
                    }
                } else {
                    logger.log(Level.INFO, "============DATASIZE_Query from==========" + pageSize + "========");
                    data = cmisService.find(searchQuery);
                    logger.log(Level.INFO, "============DATASIZE_Query to==========" + pageSize + "========");
                    return data;
                }
            }
        }
        // for advaced search
        else {
            int dataSize = cmisService.getMaxNumberOfRowsByQuery(searchQueryAdvanced);
            this.setRowCount(dataSize);

            if (dataSize > pageSize) {
                if ((first + pageSize) > dataSize) {
                    logger.log(Level.INFO, "============IN FIND====31==============");
                    data = cmisService.getPageForLazySearchQuery(first, dataSize - first, searchQueryAdvanced);
                    return data;
                } else {
                    logger.log(Level.INFO, "============IN FIND====32==============");
                    data = cmisService.getPageForLazySearchQuery(first, pageSize, searchQueryAdvanced);
                    return data;
                }
            } else {
                logger.log(Level.INFO, "============IN FIND====4==============");
                data = cmisService.find(searchQuery);
                return data;
            }

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