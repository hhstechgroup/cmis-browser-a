package com.engagepoint.labs.web.models;

import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.util.ArrayList;
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
public class LazyFSObjectDataModel extends LazyDataModel<FSObject> {

    private static Logger logger = Logger.getLogger(LazyFSObjectDataModel.class.getName());

    private FSFolder parent;
    private CMISService cmisService;

    public LazyFSObjectDataModel(CMISService cmisService, FSFolder parent) {
        this.cmisService = cmisService;
        this.parent = parent;
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
    public List<FSObject> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String,String> filters) {
        logger.log(Level.INFO, "===============================List<FSObject> load fake ========");
        return null;
    }

    @Override
    public List<FSObject> load(int first, int pageSize, String sortField, org.primefaces.model.SortOrder sortOrder, Map<String, String> filters) {
        List<FSObject> data = new ArrayList<FSObject>();
        logger.log(Level.INFO, "===============================List<FSObject> load   first= " + first + "===pagesize = " + pageSize + "======");
        //filter
//        for(FSObject fsObject : datasource) {
//            boolean match = true;
//
//            for(Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
//                try {
//                    String filterProperty = it.next();
//                    String filterValue = filters.get(filterProperty);
//                    String fieldValue = String.valueOf(fsObject.getClass().getField(filterProperty).get(fsObject));
//
//                    if(filterValue == null || fieldValue.startsWith(filterValue)) {
//                        match = true;
//                    }
//                    else {
//                        match = false;
//                        break;
//                    }
//                } catch(Exception e) {
//                    match = false;
//                }
//            }
//
//            if(match) {
//                data.add(fsObject);
//            }
//        }

//        //sort
//        if(sortField != null) {
//            Collections.sort(data, new LazySorter(sortField, sortOrder));
//        }

//        //rowCount
//        int dataSize = data.size();
//        this.setRowCount(dataSize);

        int dataSize = cmisService.getMaxNumberOfRows(parent, pageSize);
        this.setRowCount(dataSize);
        logger.log(Level.INFO, "============DATASIZE==========" + dataSize + "========");

        if (dataSize > pageSize) {
            data = cmisService.getPageForLazy(parent, first, pageSize);
            return data;
        }
        else {
            data = cmisService.getPageForLazy(parent, 0 , pageSize);
            return data;
        }
    }


}