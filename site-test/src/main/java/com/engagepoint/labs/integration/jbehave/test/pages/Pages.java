package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverProvider;

/**
 * User: vitaliy.vasilenko
 * Date: 6/21/13
 * Time: 12:40 PM
 */
public class Pages {

    private final WebDriverProvider driverProvider;
    private Home home;
    private Index index;

    public Pages(WebDriverProvider driverProvider) {
        this.driverProvider = driverProvider;
    }

    public Home home() {
        if (home == null) {
            home = new Home(driverProvider);
        }
        return home;
    }

    public Index index() {
        if (index == null) {
            index = new Index(driverProvider);
        }
        return index;
    }


}