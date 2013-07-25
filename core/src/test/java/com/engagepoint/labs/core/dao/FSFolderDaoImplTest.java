package com.engagepoint.labs.core.dao;

import org.junit.Test;

/**
* Created with IntelliJ IDEA.
* User: r.reznichenko
* Date: 6/18/13
* Time: 12:15 PM
* To change this template use File | Settings | File Templates.
*/



import com.engagepoint.labs.core.models.FSFolder;
import com.engagepoint.labs.core.models.FSObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FSFolderDaoImplTest {

    private static FSFolderDaoImpl fsFolderDao;
    private FSFolder actual;
    private static Session session;
    private FSFolder parent;

    @BeforeClass
    public static void setUPclass() throws Exception {
        fsFolderDao = new FSFolderDaoImpl();
        session = ConnectionFactory.getInstance().getSession();
        fsFolderDao.setSession(session);
    }

    @Before
    public void setUP() throws Exception {
        parent = new FSFolder();
        parent.setPath("/");
        actual = fsFolderDao.create(parent, "junit_test_folder");
    }

    @Test
    public void testCreate() throws Exception {
        FSFolder expected = new FSFolder();
        expected.setPath("/junit_test_folder");
        expected.setName("junit_test_folder");
        expected.setTypeId(actual.getTypeId());
        expected.setId(actual.getId());
        expected.setParent(parent);
        assertEquals(expected, actual);
        fsFolderDao.delete(actual);
    }

    @Test
    public void testRename() throws Exception {
        String newName = "folder_2";
        FSFolder renamed = fsFolderDao.rename(actual, newName);
        assertEquals(newName, renamed.getName());
        fsFolderDao.delete(actual);
    }

    @Test
    public void testGetChildren() throws Exception {
        FSFolder test = fsFolderDao.create(actual, "test");
        Folder cmisTest = (Folder) ConnectionFactory.getInstance().getSession().getObjectByPath(test.getPath());
        FSFolder expected = new FSFolder();
        expected.setPath("/junit_test_folder/test");
        expected.setId(cmisTest.getId());
        expected.setTypeId(test.getTypeId());
        expected.setParent(actual);
        expected.setName("test");

        List<FSObject> actualList = fsFolderDao.getChildren(actual);
        FSFolder act = (FSFolder) actualList.get(0);
        assertEquals(expected, act);
        fsFolderDao.delete(test);
        fsFolderDao.delete(actual);
    }

    @Test
    public void testDelete() throws Exception {
        boolean actualBool = fsFolderDao.delete(actual);
        assertTrue(actualBool);
    }

    @Test
    public void testGetRoot() throws Exception {
        FSFolder root = fsFolderDao.getRoot();
        String expectedPath = "/";
        String actualPath = root.getPath();
        assertEquals(expectedPath, actualPath);
        fsFolderDao.delete(actual);
    }

//    @Test
//    public void testCopyFolder() throws Exception {
//        Folder source = (Folder) session.getObject("101");
//        Folder target = (Folder) session.getObject("117");
//        fsFolderDao.copyFolder(source.getId(),target.getId());
//        fsFolderDao.delete(actual);
//    }
}
