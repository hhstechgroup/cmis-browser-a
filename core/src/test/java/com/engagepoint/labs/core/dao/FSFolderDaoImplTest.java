package com.engagepoint.labs.core.dao;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FSFolderDaoImplTest {

    private static FSFolderDaoImpl fsFolderDao;
    private static ConnectionFactory connection;
    private FSFolder actual;

    private FSFolder parent;

    @BeforeClass
    public static void setUPclass() throws Exception {
        fsFolderDao = new FSFolderDaoImpl();
        fsFolderDao.setSession(ConnectionFactory.getSession());
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
        expected.setType("Folder");
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
        Folder cmisTest = (Folder) ConnectionFactory.getSession().getObjectByPath(test.getPath());
        FSFolder expected = new FSFolder();
        expected.setPath("/junit_test_folder/test");
        expected.setId(cmisTest.getId());
        expected.setType("Folder");
        expected.setParent(actual);
        expected.setName("test");

        System.out.println("name: " + expected.getName() + " type: " + expected.getType() + " path: " + expected.getPath()
                + "\nparent name: " + expected.getParent().getName() + " id: " + expected.getId());

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


}

