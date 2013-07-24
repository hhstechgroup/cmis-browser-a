package com.engagepoint.labs.web.controllers;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.BrowserRuntimeException;
import com.engagepoint.labs.core.models.exceptions.ConnectionException;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import com.engagepoint.labs.web.models.LazyFSObjectDataModel;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: bogdan.ezapenkin
 * Date: 6/17/13
 * Time: 4:37 PM
 */

@ManagedBean(name = "treeBean")
@SessionScoped
public class TreeBean implements Serializable {

    @ManagedProperty(value = "#{fileActions}")
    private FileActions fileActions;

    private TreeNode main;
    private TreeNode selectedNodes;
    private TreeNode cachedNode;
    private FSObject selectedFSObject;
    private boolean checkThatSelected;
    private FSFolder parent = new FSFolder();
    private CMISService cmisService;
    private static Logger logger = Logger.getLogger(TreeBean.class.getName());

    private List<PageState> backHistory = new LinkedList<PageState>();
    private List<PageState> forwardHistory = new LinkedList<PageState>();
    private PageState currentPageState;

    private LazyFSObjectDataModel lazyModel;

    private boolean first = true;

    private String folderType;
    private Map<String, String> folderTypes = new HashMap<String, String>();


    private String findQuery;
    private Map<Integer, Object> searchQueryAdvanced;
    private String cmisType;
    private String metaDataType;
    private String docType;
    private Date calendarFrom;
    private Date calendarTo;
    private String contentType;
    private String sizeFrom;
    private String sizeTo;
    private String snippet;
    private boolean disableSearchFolderProperties;
    private boolean disableSearchSimple;
    private boolean ableSearchAdvanced;
    private String searchPropertiesVisibility;

    public String getFolderType() {
        return folderType;
    }

    public void setFolderType(String folderType) {
        this.folderType = folderType;
    }


    public Map<String, String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(Map<String, String> folderTypes) {
        this.folderTypes = folderTypes;
    }


    public TreeBean() {
        try {
            cmisService = CMISServiceImpl.getService();
        } catch (ConnectionException e) {
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
        searchQueryAdvanced = new HashMap<Integer, Object>();
        searchQueryAdvanced.put(0, "cmis:document");
        ableSearchAdvanced = false;
        cmisType = "cmis:document";
    }

    public void drawComponent(){
        FSFolder root = cmisService.getRootFolder();
        parent.setPath("/");
        main = new DefaultTreeNode("Main", null);
        TreeNode node0 = new DefaultTreeNode(root, main);
        FSFolder fold = new FSFolder();
        fold.setName("Empty Folder");
        new DefaultTreeNode(fold, node0);
        this.selectedNodes = node0;
        changedTableParentFolder();
        folderTypes.put("CMIS Folder (cmis:folder)", "CMIS Folder (cmis:folder)");

        searchQueryAdvanced = new HashMap<Integer, Object>();
        searchQueryAdvanced.put(0, "cmis:document");
        ableSearchAdvanced = false;
        cmisType = "cmis:document";
    }

    public void updateTree(TreeNode parent) {

        parent.getChildren().clear();
//        logger.log(Level.INFO, "Cast " + (String) parent.getData());
        try {
            List<FSObject> children = cmisService.getChildren((FSFolder) parent.getData());

            for (FSObject child : children) {
                if (child instanceof FSFolder) {
                    TreeNode treeNode = new DefaultTreeNode(child, parent);
                    long start = System.currentTimeMillis();
                    if (cmisService.hasChildFolder((FSFolder) treeNode.getData())) {
                        FSFolder fold = new FSFolder();
                        fold.setName("Empty Folder");
                        new DefaultTreeNode(fold, treeNode);
                        //                    logger.log(Level.INFO, "FOLDER " + ((FSFolder) treeNode.getData()).getName() + " HAS CHILD FOLDER");
                    }
                    long end = System.currentTimeMillis();
                    //                logger.log(Level.INFO, "TIME: " + (end - start) + "ms");
                }
            }
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "updateTree catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
    }

    public void doBack() {
        logger.log(Level.INFO, "doBack");
        if (backHistory.size() == 0) {
            return;
        }
        for (int i = 0; i < backHistory.size(); i++) {
            logger.log(Level.INFO, "BEFORE doBack REMOVE name: " + backHistory.get(i).getSelectedObject().getName());
        }
        if (!first) {
            backHistory.remove(0);
        }
        first = false;
        currentPageState = backHistory.remove(0);
        addToForward(currentPageState);
        currentPageState = backHistory.get(0);
        for (int i = 0; i < backHistory.size(); i++) {
            logger.log(Level.INFO, "AFTER doBack REMOVE name: " + backHistory.get(i).getSelectedObject().getName());
        }
        updateBean(currentPageState);
    }

    public void doForward() {
        if (forwardHistory.size() == 0) {
            return;
        }
        for (int i = 0; i < forwardHistory.size(); i++) {
            logger.log(Level.INFO, "BEFORE REMOVE name: " + forwardHistory.get(i).getSelectedObject().getName());
        }
        currentPageState = forwardHistory.remove(0);
        backHistory.remove(0);
        logger.log(Level.INFO, "REMOVE name: " + currentPageState.getSelectedObject().getName());
        for (int i = 0; i < forwardHistory.size(); i++) {
            logger.log(Level.INFO, "AFTER REMOVE name: " + forwardHistory.get(i).getSelectedObject().getName());
        }
        updateBean(currentPageState);
        addToBack(currentPageState);
    }

    public void addToBack(PageState state) {
        logger.log(Level.INFO, "addToBack name: " + state.getSelectedObject().getName());
        backHistory.add(0, state);
    }

    public void addToForward(PageState state) {
        logger.log(Level.INFO, "addToForward name: " + state.getSelectedObject().getName());
        forwardHistory.add(0, state);
    }

    public void updateBean(PageState pageState) {
        logger.log(Level.INFO, "updateBean");
//        this.currentPage = pageState.getCurrentPage();
        this.selectedFSObject = pageState.getSelectedObject();
        this.selectedNodes = pageState.getSelectedNode();
        this.parent.setPath(pageState.getParentpath());
        this.selectedNodes.setSelected(true);
//        updatetablePageList();
    }

    public void onNodeExpand(NodeExpandEvent event) {
        updateTree(event.getTreeNode());
    }

    /**
     * if files in our repository has changed when node collapse
     * we clear all children and check if node has children
     *
     * @param event that is fired on NodeCollapse
     */
    public void onNodeCollapse(NodeCollapseEvent event) {
        logger.log(Level.INFO, "onNodeCollapse");
        event.getTreeNode().getChildren().clear();
        try {
            if (cmisService.hasChildFolder((FSFolder) event.getTreeNode().getData())) {
                FSFolder fold = new FSFolder();
                fold.setName("Empty Folder");
                new DefaultTreeNode(fold, event.getTreeNode());
            }
        } catch (ConnectionException e) {
            logger.log(Level.INFO, "onNodeCollapse catched: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Connection error!",
                    "Connection lost!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
        }
    }

    public void changedTableParentFolder() {
        lazyModel = new LazyFSObjectDataModel(cmisService, parent);
    }

    public void setSelectedNode(TreeNode selectedNodes) {
        logger.log(Level.INFO, "setSelectedNode");
        if (selectedNodes != null) {
            this.selectedNodes = selectedNodes;
            logger.log(Level.INFO, "SelectedFSObject  = " + ((FSObject) selectedNodes.getData()).getName());
            setSelectedFSObject((FSObject) selectedNodes.getData());
            if (selectedFSObject.getPath() == null) {
                parent.setPath("/");
                selectedFSObject.setPath("/");
            } else {
                parent.setPath(selectedFSObject.getPath());
            }
//            if (findQuery.isEmpty()) {
//                changedTableParentFolder();
//            }
            PageState state = new PageState();
//            state.setCurrentPage(currentPage);
            state.setSelectedNode(selectedNodes);
            state.setSelectedObject(selectedFSObject);
            state.setParentpath(parent.getPath());
            addToBack(state);
            this.cachedNode = selectedNodes;
            this.selectedNodes.setSelected(false);
        }
    }

    public void onRowSelect(SelectEvent event) {
        if (selectedNodes != null) {
            this.cachedNode = selectedNodes;
            this.selectedNodes.setSelected(false);
        }
        setSelectedFSObject((FSObject) event.getObject());
    }

    public void onDragDrop(TreeDragDropEvent event) {
        FSFolder dragedFolder = (FSFolder) event.getDragNode().getData();
        FSFolder dropedFolder = null;
        try {
            dropedFolder = (FSFolder) event.getDropNode().getData();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "You cant move into this folder!",
                    "It is a terrible idea!"));
            updateTree(getRoot().getChildren().get(0));
        }
        try {
            cmisService.move(dragedFolder, dropedFolder);
        } catch (BrowserRuntimeException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "This name already exists in folder!"));
            updateTree(getRoot().getChildren().get(0));
        }
        updateTree(event.getDropNode());

    }

    public boolean isCheckThatSelected() {
        return selectedFSObject != null ? true : false;
    }

    public TreeNode getRoot() {
        return main;
    }

    public TreeNode getSelectedNode() {
        return selectedNodes;
    }

    public FSObject getSelectedFSObject() {
        return selectedFSObject;
    }

    public void setSelectedFSObject(FSObject sn) {
        if (sn != null) {
            if (sn instanceof FSFile) {
                fileActions.setSelectedIsFile(true);
                fileActions.setVersionable(((FSFile) sn).isVersionable());
            } else {
                fileActions.setSelectedIsFile(false);
            }
            fileActions.setSelectedName(sn.getName());
            this.selectedFSObject = sn;
            logger.log(Level.INFO, "selectedObject: " + sn.getName());
        }
    }

    public FSFolder getParent() {
        return parent;
    }

    public void setParent(FSFolder parent) {
        this.parent = parent;
    }

    public FileActions getFileActions() {
        return fileActions;
    }

    public void setFileActions(FileActions fileActions) {
        this.fileActions = fileActions;
    }

    public LazyFSObjectDataModel getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyFSObjectDataModel lazyModel) {
        this.lazyModel = lazyModel;
    }

    public String getFindQuery() {
        logger.log(Level.INFO, "get=" + findQuery);
        return findQuery;
    }

    public void setFindQuery(String findQuery) {
        if (findQuery == null) {
            this.findQuery = "";
            getLazyModel().setSearchQuery(findQuery);
            logger.log(Level.INFO, "setFIndQuery=nulllll" + findQuery);
        } else {
            this.findQuery = findQuery;
            getLazyModel().setSearchQuery(findQuery);
            logger.log(Level.INFO, "setFindQuery=-------------- " + getLazyModel().getSearchQuery());

        }
    }

    public void findObjects() {
        logger.log(Level.INFO, "find=" + cmisService.find(findQuery).get(0).getName());
        // treeBean.updatetablePageList(cmisService.find(findQuery));
    }

    public void findAdvanced() {

        logger.log(Level.INFO, "==___________findAdvanced()____");


        if (findQuery != "") {
            searchQueryAdvanced.put(1, "%" + findQuery + "%");
        } else {
            searchQueryAdvanced.put(1, findQuery);
        }

        if (metaDataType != null) {
            searchQueryAdvanced.put(2, metaDataType);
        } else {
            searchQueryAdvanced.put(2, "");
        }
        if (docType != "") {
            searchQueryAdvanced.put(3, "%." + docType);
        } else {
            searchQueryAdvanced.put(3, docType);
        }
        searchQueryAdvanced.put(4, snippet);

        if (contentType != null) {
            searchQueryAdvanced.put(5, contentType);
        } else {
            searchQueryAdvanced.put(5, "");
        }
        searchQueryAdvanced.put(6, calendarFrom);
        searchQueryAdvanced.put(7, calendarTo);
        searchQueryAdvanced.put(8, sizeFrom);
        searchQueryAdvanced.put(9, sizeTo);
        logger.log(Level.INFO, "==______BEFORE_____BOOOOOOOOOOOOOOOM~!!!!____");
        getLazyModel().setSearchQueryAdvanced(searchQueryAdvanced);

        if(ableSearchAdvanced){
            logger.log(Level.INFO, "==___________B1111111111111111111111M~!!!!____"+searchQueryAdvanced);
            getLazyModel().setAbleSearchAdvanced(true);
        } else {
            logger.log(Level.INFO, "==___________BssssssssssssssssssssssM~!!!!____"+searchQueryAdvanced);
            getLazyModel().setAbleSearchAdvanced(false);
        }
        logger.log(Level.INFO, "==___________BOOOOOOOOOOOOOOOM~!!!!____"+searchQueryAdvanced);
        logger.log(Level.INFO, "==___________BOOOOOOOOOOOOOOOM~!!!!____");
    }

    public void openSearchAdvanced() {
        searchQueryAdvanced.put(0, "cmis:document");
    }

    public void ableSearchAdvanced() {
        ableSearchAdvanced = true;
        logger.log(Level.INFO, "ableSearchAdvanced = "+ableSearchAdvanced);
//        searchPropertiesVisibility = "visible";
//        lazyModel.setAbleSearchAdvanced(true);
    }
    public void disableSearchAdvanced() {
        ableSearchAdvanced = false;
        logger.log(Level.INFO, "disableSearchAdvanced = "+ableSearchAdvanced);
//        searchPropertiesVisibility = "hidden";
//        lazyModel.setAbleSearchAdvanced(false);
    }

    public void choosenCmisType() {
        searchQueryAdvanced.put(0, cmisType);
        if (cmisType.equals("cmis:folder")) {
            disableSearchFolderProperties = true;
        } else {
            disableSearchFolderProperties = false;
        }
    }

    public Map<Integer, Object> getSearchQueryAdvanced() {
        return searchQueryAdvanced;
    }

    public Date getCalendarTo() {
        return calendarTo;
    }

    public void setCalendarTo(Date calendarTo) {
        this.calendarTo = calendarTo;
        logger.log(Level.INFO, "___________calendar format___" + this.calendarFrom + ">");

    }

    public Date getCalendarFrom() {
        return calendarFrom;
    }

    public void setCalendarFrom(Date calendarFrom) {
        this.calendarFrom = calendarFrom;

        logger.log(Level.INFO, "___________calendar format___" + this.calendarFrom + ">");
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        if (snippet != null) {
            this.snippet = snippet;
        } else {
            this.snippet = "";
        }
    }

    public String getSizeTo() {
        return sizeTo;
    }

    public void setSizeTo(String sizeTo) {
        if (sizeTo != null) {
            this.sizeTo = sizeTo;
        } else {
            this.sizeTo = "";
        }
    }

    public String getSizeFrom() {
        return sizeFrom;
    }

    public void setSizeFrom(String sizeFrom) {
        if (sizeFrom != null) {
            this.sizeFrom = sizeFrom;
        } else {
            this.sizeFrom = "";
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        if (contentType != null) {
            this.contentType = contentType;
        } else {
            this.contentType = "";
        }
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        if (docType != null) {
            this.docType = docType;
        } else {
            this.docType = "";
        }
    }

    public String getMetaDataType() {
        return metaDataType;
    }

    public void setMetaDataType(String metaDataType) {
        if (metaDataType != null) {
            this.metaDataType = metaDataType;
        } else {
            this.metaDataType = "";
        }
    }

    public String getCmisType() {
        return cmisType;
    }

    public void setCmisType(String cmisType) {
        if (cmisType != null) {
            this.cmisType = cmisType;
        } else {
            this.cmisType = "";
        }
    }

    public boolean isDisableSearchFolderProperties() {
        return disableSearchFolderProperties;
    }

    public void setDisableSearchFolderProperties(boolean disableSearchFolderProperties) {
        this.disableSearchFolderProperties = disableSearchFolderProperties;
    }

    public boolean isDisableSearchSimple() {
        return disableSearchSimple;
    }

    public void setDisableSearchSimple(boolean disableSearchSimple) {
        this.disableSearchSimple = disableSearchSimple;
    }

    public TreeNode getCachedNode() {
        return cachedNode;
    }

    public void setCachedNode(TreeNode cachedNode) {
        logger.log(Level.INFO, "SetCachedNode");
        if (cachedNode != null)
            this.cachedNode = cachedNode;
    }

    public String getSearchPropertiesVisibility() {
        return searchPropertiesVisibility;
    }

    public void setSearchPropertiesVisibility(String searchPropertiesVisibility) {
        this.searchPropertiesVisibility = searchPropertiesVisibility;
    }

    public boolean isAbleSearchAdvanced() {
        return ableSearchAdvanced;
    }
}