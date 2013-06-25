package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSFile;
import com.engagepoint.labs.core.models.FSFolder;
import org.apache.chemistry.opencmis.client.api.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: r.reznichenko
 * Date: 6/18/13
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class FSFileDaoImplTest {

    private static FSFileDao fsFileDao;
    private static ConnectionFactory connection;

    private FSFolder parent;
    private String name;
    private String content;
    private FSFile created;

    @BeforeClass
    public static void setUPclass() throws Exception {
        fsFileDao = new FSFileDaoImpl();
        fsFileDao.setSession(ConnectionFactory.getSession());
    }

    @Before
    public void setUp() throws Exception {
        parent = new FSFolder();
        parent.setPath("/My_Folder-0-1");
        name = "test.txt";
        content = "test.content";
    }

    @After
    public void clean() throws Exception {

    }

    @Test
    public void testCreate() throws Exception {
        created = fsFileDao.create(parent, name, content);
        Document doc = (Document) connection.getSession().getObjectByPath("/My_Folder-0-1/"+name);
        FSFile expected = new FSFile();
        expected.setId(doc.getId());
        expected.setName(doc.getName());
        expected.setPath("/My_Folder-0-1/" + name);
        expected.setContent(content);
        expected.setParent(parent);
        expected.setType(doc.getType().getDisplayName());
        assertEquals(expected, created);
        fsFileDao.delete(created);
    }

    @Test
    public void testRename() throws Exception {
        created = fsFileDao.create(parent, name, content);
        String newFileName =  "qwerty.txt";
        created = fsFileDao.rename(created, newFileName);
        assertEquals(newFileName, created.getName());
        fsFileDao.delete(created);
    }

    @Test
    public void testGetContent() throws Exception {
        name = "qwerty.txt";
        created = fsFileDao.create(parent, name, content);
        String expectedContent = "test.content";
        String actual = fsFileDao.getContent(created);
        assertEquals(expectedContent, actual);
        fsFileDao.delete(created);
    }

    @Test
    public void testDelete() throws Exception {
        name = "qwerty.txt";
        created = fsFileDao.create(parent, name, content);
        boolean check = fsFileDao.delete(created);
        assertTrue(check);
    }
}