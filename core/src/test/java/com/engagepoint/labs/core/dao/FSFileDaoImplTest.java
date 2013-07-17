package com.engagepoint.labs.core.dao;

import com.engagepoint.labs.core.models.FSObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: r.reznichenko
 * Date: 6/18/13
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class FSFileDaoImplTest {

    private static FSFolderDao fsFolderDao;
    private static FSFileDao fsFileDao;
    private static Session session;
    private static Logger logger = Logger.getLogger(FSFileDaoImplTest.class.getName());

    @BeforeClass
    public static void setUPclass() throws Exception {
        fsFolderDao = new FSFolderDaoImpl();
        fsFileDao = new FSFileDaoImpl();
        session = ConnectionFactory.getSession();
        fsFolderDao.setSession(session);
        fsFileDao.setSession(session);
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void clean() throws Exception {

    }

//    @Test
//    public void testVersion() {
//        Document doc = (Document) session.getObject("180");
////        doc.
//        List<Document> list = doc.getAllVersions();
//        System.out.println(list.size());
//        for(Document o : list) {
//            System.out.println("Name: "+o.getName()+" Label: "+o.getVersionLabel()+" Latest: " + o.isLatestVersion()
//                    +" Latest Major: " + o.isLatestMajorVersion() + " Major: " + o.isMajorVersion());
//        }
//    }

    @Test
    public void testFind() throws Exception{
        List<FSObject> fsFiles = fsFileDao.find("d");
        System.out.println(fsFiles.size());
        for (FSObject fsObject : fsFiles){
            System.out.println(fsObject.getName());
        }
        assertNotNull(fsFiles);
    }

//    @org.junit.Test
//         public void testCopy() throws Exception {
//        Document doc = (Document) session.getObject("133");
//        FSFile file = new FSFile();
//        file.setName(doc.getName());
//        file.setId(doc.getId());
//        file.setAbsolutePath(doc.getPaths().get(0));
//        System.out.println(file.getAbsolutePath());
//        fsFolderDao.getFsFileDao().copy(file.getId(), "117");
//        doc = (Document) session.getObject("229");
//        String expectedPath = "/My_Folder-0-1/"+file.getName();
//        assertEquals(expectedPath, doc.getPaths().get(0));
//    }


}