package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverProvider;
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

/**
 * User: vitaliy.vasilenko
 * Date: 6/21/13
 * Time: 12:40 PM
 */
public class Home extends AbstractPage {

    public Home(WebDriverProvider driverProvider) {
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

    private void sleepOnJBehave() {
        try {
            Thread.currentThread().sleep(1000);//1sec
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}