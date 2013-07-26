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
import org.primefaces.context.RequestContext;
import org.primefaces.event.*;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

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
    private FSFolder parent = new FSFolder();
    private CMISService cmisService;

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
    private int sizeMultiplier;

    private boolean rowSelected;

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
        findQuery = "";
        sizeMultiplier = 1024;

        rowSelected = false;
    }

    public void drawComponent() {
        FSFolder root = cmisService.getRootFolder();
        parent.setPath("/");
        parent.setId(root.getId());
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
        try {
            List<FSObject> children = cmisService.getChildren((FSFolder) parent.getData());

            for (FSObject child : children) {
                if (child instanceof FSFolder) {
                    TreeNode treeNode = new DefaultTreeNode(child, parent);
                    if (cmisService.hasChildFolder((FSFolder) treeNode.getData())) {
                        FSFolder fold = new FSFolder();
                        fold.setName("Empty Folder");
                        new DefaultTreeNode(fold, treeNode);
                    }
                }
            }
        } catch (ConnectionException e) {
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
        if (backHistory.size() == 0) {
            return;
        }
        for (int i = 0; i < backHistory.size(); i++) {
        }
        if (!first) {
            backHistory.remove(0);
        }
        first = false;
        currentPageState = backHistory.remove(0);
        addToForward(currentPageState);
        currentPageState = backHistory.get(0);
        for (int i = 0; i < backHistory.size(); i++) {
        }
        updateBean(currentPageState);
    }

    public void doForward() {
        if (forwardHistory.size() == 0) {
            return;
        }
        for (int i = 0; i < forwardHistory.size(); i++) {
        }
        currentPageState = forwardHistory.remove(0);
        backHistory.remove(0);
        for (int i = 0; i < forwardHistory.size(); i++) {
        }
        updateBean(currentPageState);
        addToBack(currentPageState);
    }

    public void addToBack(PageState state) {
        backHistory.add(0, state);
    }

    public void addToForward(PageState state) {
        forwardHistory.add(0, state);
    }

    public void updateBean(PageState pageState) {
        this.selectedFSObject = pageState.getSelectedObject();
        this.selectedNodes = pageState.getSelectedNode();
        this.parent.setPath(pageState.getParentpath());
        this.selectedNodes.setSelected(true);
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
        event.getTreeNode().getChildren().clear();
        try {
            if (cmisService.hasChildFolder((FSFolder) event.getTreeNode().getData())) {
                FSFolder fold = new FSFolder();
                fold.setName("Empty Folder");
                new DefaultTreeNode(fold, event.getTreeNode());
            }
        } catch (ConnectionException e) {
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
        if (selectedNodes != null) {
            try {
                this.selectedNodes = selectedNodes;
                setSelectedFSObject((FSObject) selectedNodes.getData());
                if (selectedFSObject.getPath() == null) {
                    parent.setPath("/");
                    parent.setId("100");
                    selectedFSObject.setPath("/");
                } else {
                    parent.setPath(selectedFSObject.getPath());
                    parent.setId(selectedFSObject.getId());
                }
                if (findQuery.isEmpty() && !ableSearchAdvanced) {
                    changedTableParentFolder();
                }
                PageState state = new PageState();
                state.setSelectedNode(selectedNodes);
                state.setSelectedObject(selectedFSObject);
                state.setParentpath(parent.getPath());
                addToBack(state);
                this.cachedNode = selectedNodes;
                this.selectedNodes.setSelected(false);
                setRowSelected(false);
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Connection error!",
                        e.getMessage()));
            }
        }
    }

    public void onRowSelect(SelectEvent event) {
        setRowSelected(true);
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
            drawComponent();
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
        return findQuery;
    }

    public void setFindQuery(String findQuery) {
        if (findQuery == null) {
            this.findQuery = "";
            getLazyModel().setSearchQuery(findQuery);
        } else {
            this.findQuery = findQuery;
            getLazyModel().setSearchQuery(findQuery);

        }
    }

    public void findAdvanced() {
        if (findQuery != "" && findQuery != null) {
            searchQueryAdvanced.put(1, "%" + findQuery + "%");
        } else {
            searchQueryAdvanced.put(1, "");
        }

        if (metaDataType != "" && metaDataType != null && cmisType.equals("cmis:document")) {
            searchQueryAdvanced.put(2, "%" + metaDataType + "%");
        } else {
            searchQueryAdvanced.put(2, "");
        }
        if (docType != "" && docType != null && cmisType.equals("cmis:document")) {
            searchQueryAdvanced.put(3, "%" + docType + "%");
        } else {
            searchQueryAdvanced.put(3, "");
        }
        if (snippet != "" && snippet != null && cmisType.equals("cmis:document")) {
            searchQueryAdvanced.put(4, snippet);
        } else {
            searchQueryAdvanced.put(4, "");
        }

        if (contentType != null && contentType != "") {
            searchQueryAdvanced.put(5, contentType);
        } else {
            searchQueryAdvanced.put(5, "");
        }
        searchQueryAdvanced.put(6, calendarFrom);
        searchQueryAdvanced.put(7, calendarTo);
        try {
            Integer.parseInt(sizeFrom);
            searchQueryAdvanced.put(8, Integer.toString(Integer.parseInt(sizeFrom) * sizeMultiplier));
        } catch (NumberFormatException e) {
            searchQueryAdvanced.put(8, "");
        }
        try {
            Integer.parseInt(sizeTo);
            searchQueryAdvanced.put(9, Integer.toString(Integer.parseInt(sizeTo) * sizeMultiplier));
        } catch (NumberFormatException e) {
            searchQueryAdvanced.put(9, "");
        }
        getLazyModel().setSearchQueryAdvanced(searchQueryAdvanced);
        if (ableSearchAdvanced) {
            getLazyModel().setAbleSearchAdvanced(true);
        } else {
            getLazyModel().setAbleSearchAdvanced(false);
        }
    }

    public void openSearchAdvanced() {
        searchQueryAdvanced.put(0, "cmis:document");
    }

    public void ableSearchAdvanced() {
        ableSearchAdvanced = true;
    }

    public void disableSearchAdvanced() {
        ableSearchAdvanced = false;
    }

    public void choosenCmisType() {
        searchQueryAdvanced.put(0, cmisType);
        if (cmisType.equals("cmis:folder")) {
            disableSearchFolderProperties = true;
        } else {
            disableSearchFolderProperties = false;
        }
    }

    public void onDragFile(DragDropEvent event) {
        FSObject fsObject = null;
        try {
            fsObject = (FSObject) event.getData();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "You cant move into this folder!",
                    "It is a terrible idea!"));
        }
        try {
            if (fsObject instanceof FSFile) {
                cmisService.move((FSFile) fsObject);
            } else {
                RequestContext.getCurrentInstance().update(":cmisbrowser:treeForm:table:tablecomponent");
                throw new BaseException("You cant move folder from table!");
            }
        } catch (BrowserRuntimeException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    "This name already exists in folder!"));
        } catch (BaseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    e.getMessage(),
                    ""));
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

    }

    public Date getCalendarFrom() {
        return calendarFrom;
    }

    public void setCalendarFrom(Date calendarFrom) {
        this.calendarFrom = calendarFrom;
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

    public boolean isRowSelected() {
        return rowSelected;
    }

    public void setRowSelected(boolean rowSelected) {
        this.rowSelected = rowSelected;
    }

    public int getSizeMultiplier() {
        return sizeMultiplier;
    }

    public void setSizeMultiplier(int sizeMultiplier) {
        this.sizeMultiplier = sizeMultiplier;
    }
}