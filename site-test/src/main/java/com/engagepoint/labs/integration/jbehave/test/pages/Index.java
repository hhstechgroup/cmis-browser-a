package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverProvider;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: iryna.domachuk
 * Date: 6/21/13
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
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
