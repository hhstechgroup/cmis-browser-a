package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverProvider;
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

/**
 * User: vitaliy.vasilenko
 * Date: 6/21/13
 * Time: 12:40 PM
 */
public class Index extends AbstractPage  {

    public Index(WebDriverProvider driverProvider) {
        super(driverProvider);
    }

    public void open() {
        get("http://localhost:8080/site/index.xhtml"); //  --- > url in story
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }

    public void treeExpand() {

        findElement(By.xpath(".//*[@id='treeForm:tree:0']/span/span[1]")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }

    public void treeClick() {

        findElement(By.xpath(".//*[@id='treeForm:tree:0']/span/span[3]")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }

    public void treeClickCheck() {

        findElement(By.xpath(".//*[@id='treeForm:tree:0'][@aria-selected='true']"));

    }

    public void treeExpandCheck() {

        findElement(By.xpath(".//*[@id='treeForm:tree:0' and @aria-expanded='true']"));

    }

    public void deleteButtonClickForFolder() {

        findElement(By.xpath(".//*[@id='treeForm:fsTable:j_idt39']")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }

    public void cancelButtonClickOnDeleteFolderPage() {

        findElement(By.xpath(".//*[@id='j_idt68:j_idt73']")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }
    public void createButtonClickForFolder() {

        findElement(By.xpath(".//*[@id='treeForm:fsTable:j_idt36']")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }
    public void cancelButtonClickOnCreatePage() {

        findElement(By.xpath(".//*[@id='createForm:j_idt67']")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }
}
