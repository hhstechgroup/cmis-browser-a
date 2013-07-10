package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverProvider;
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

/**
 * User: vitaliy.vasilenko
 * Date: 6/21/13
 * Time: 12:40 PM
 */
public class Index extends AbstractPage {

    public Index(WebDriverProvider driverProvider) {
        super(driverProvider);
    }

    public void open() {
        get("http://localhost:8080/site/index.xhtml"); //  --- > url in story
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void treeExpand() {
        findElement(By.xpath(".//*[@id='treeForm:tree:0']/span/span[1]")).click();
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void treeClick() {
        findElement(By.xpath(".//*[@id='treeForm:tree:0']/span/span[3]")).click();
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void treeClickCheck() {
        findElement(By.xpath(".//*[@id='treeForm:tree:0'][@aria-selected='true']"));
        sleepOnJBehave();
    }

    public void treeExpandCheck() {
        findElement(By.xpath(".//*[@id='treeForm:tree:0' and @aria-expanded='true']"));
        sleepOnJBehave();
    }

    public void deleteButtonClickForFolder() {
        findElement(By.xpath(".//*[@id='treeForm:fsTable:j_idt38']")).click();
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void cancelButtonClickOnDeleteFolderPage() {
        findElement(By.xpath(".//*[@id='j_idt67:j_idt70']")).click();
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void createNewFolder() {
        findElement(By.xpath(".//*[@id='treeForm:fsTable:j_idt36']")).click();
        sleepOnJBehave();
        findElement(By.xpath(".//*[@id='createForm:name']")).sendKeys("CreateJBehaveFolder");
        sleepOnJBehave();
        findElement(By.xpath(".//*[@id='createForm:type']")).sendKeys("Folder");
        sleepOnJBehave();
        findElement(By.xpath(".//*[@id='createForm:j_idt65']")).click();
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void createFolderAndClickCancel() {
        findElement(By.xpath(".//*[@id='treeForm:tree:0']/span/span[3]")).click();
        sleepOnJBehave();
        findElement(By.xpath(".//*[@id='treeForm:fsTable:j_idt36']")).click();
        sleepOnJBehave();
        findElement(By.xpath(".//*[@id='createForm:j_idt66']")).click();
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void deleteFolder() {
        findElement(By.xpath(".//*[@id='treeForm:tree:0_0']/span/span[3]")).click();
        sleepOnJBehave();
        findElement(By.xpath(".//*[@id='treeForm:fsTable:j_idt38']")).click();
        sleepOnJBehave();
        findElement(By.xpath(".//*[@id='j_idt67:j_idt69']")).click();
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        sleepOnJBehave();
    }

    public void selectTreeNode() {
        findElement(By.xpath(".//*[@id='treeForm:tree:0_0']/span/span[3]")).click();
        sleepOnJBehave();
    }

    private void sleepOnJBehave() {
        try {
            Thread.currentThread().sleep(1000);//1sec
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
