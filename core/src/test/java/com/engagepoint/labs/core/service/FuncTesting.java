package com.engagepoint.labs.core.service;

/**
 * @author volodymyr.kozubal <volodymyr.kozubal@engagepoint.com>
 */
import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import com.engagepoint.labs.core.service.CMISService;
import com.engagepoint.labs.core.service.CMISServiceImpl;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FuncTesting {
    @Test
    public void getFolderChildrenTest()throws Exception{
        CMISService cmisService = new CMISServiceImpl();
        FSFolder parent = new FSFolder();
        parent.setPath("/");
        cmisService.deleteAllTree(parent);

        List<String> expectedNamesList = Arrays.asList("My_Document-0-0", "My_Document-0-1",
                "My_Document-0-2", "My_Folder-0-0", "My_Folder-0-1");
        for (String foldername : expectedNamesList ){
                cmisService.createFolder(parent, foldername);

        }
        List<FSObject> getObjectsList = cmisService.getChildren(parent);
        List<String> getNamesList = new ArrayList<String>();
        for (FSObject i : getObjectsList ) getNamesList.add(i.getName());
        //the same elements in the same order
        assertThat(getNamesList, is(expectedNamesList));
    }

    @Test
    public void getRootFolderTest() {
        CMISService cmisService = new CMISServiceImpl();
        FSObject root = new FSFolder();
        root = cmisService.getRootFolder();
        String expectedPath = "/";
        assertEquals(root.getParent(), null);
        assertEquals(expectedPath, root.getPath());
    }
}
