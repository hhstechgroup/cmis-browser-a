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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

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
        expected.setType("cmis:folder");
        expected.setChildren(null);
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
        List<FSFolder> expected = new ArrayList<FSFolder>(1);

        FSFolder folder = new FSFolder();
        folder.setPath("/junit_test_folder/test");
        folder.setId("3254");
        folder.setType("cmis:folder");
        expected.add(folder);
        FSFolder test = fsFolderDao.create(actual, "test");
        List<FSObject> actualList = fsFolderDao.getChildren(actual);
        assertEquals(expected, actualList);
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

