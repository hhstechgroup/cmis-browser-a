package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverProvider;
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

public class Home extends AbstractPage {

    public Home(WebDriverProvider driverProvider) {
        super(driverProvider);
    }

    public void open() {
        get("http://localhost:8080/site/index.xhtml"); //  --- > url in story
        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    public void treeExpand() {

        findElement(By.xpath(".//*[@id='formLeftTree:tree:0']/span/span[1]")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }

    public void treeClick() {

        findElement(By.xpath(".//*[@id='formLeftTree:tree:0']/span/span[3]")).click();

        manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

    }

    public void treeClickCheck() {

        findElement(By.xpath(".//*[@id='formLeftTree:tree:0'][@aria-selected='true']"));

    }

    public void treeExpandCheck() {

        findElement(By.xpath(".//*[@id='formLeftTree:tree:0' and @aria-expanded='true']"));

    }


}