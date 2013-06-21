package com.engagepoint.labs.core.servieses;

/**
 * User: vitaliy.vasilenko
 * Date: 6/20/13
 * Time: 6:50 PM
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
    public void getChildrenTest() {
        CMISService service = new CMISServiceImpl();
        FSFolder parent = new FSFolder();
        parent.setPath("/");
        List<FSObject> getObjectsList = service.getChildren(parent);
        List<String> getNamesList = new ArrayList<String>();
        for (FSObject i : getObjectsList ) getNamesList.add(i.getName()) ;
        List<String> expectedNamesList = Arrays.asList("My_Document-0-0", "My_Document-0-1",
                "My_Document-0-2", "My_Folder-0-0", "My_Folder-0-1");
        //the same elements in the same order
        assertThat(getNamesList, is(expectedNamesList));

    }

    @Test
    public void getRootFolderTest() {
        CMISService service = new CMISServiceImpl();
        FSObject root = new FSFolder();
        root = service.getRootFolder();
        String expectedPath = "/";
        assertEquals(root.getParent(), null);
        assertEquals(expectedPath, root.getPath());
    }


}

