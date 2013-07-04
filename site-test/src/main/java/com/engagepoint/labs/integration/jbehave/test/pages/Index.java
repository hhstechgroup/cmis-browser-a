package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverProvider;

import java.util.concurrent.TimeUnit;

/**
 * User: vitaliy.vasilenko
 * Date: 7/2/13
 * Time: 6:30 PM
 */
public class Index extends AbstractPage  {

    public Index(WebDriverProvider driverProvider) {
        super(driverProvider);
    }

    public void open() {
        get("http://localhost:8080/site/index.xhtml"); //  --- > url in story
        manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

}
