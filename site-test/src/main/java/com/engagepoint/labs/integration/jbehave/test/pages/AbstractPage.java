package com.engagepoint.labs.integration.jbehave.test.pages;

import org.jbehave.web.selenium.WebDriverPage;
import org.jbehave.web.selenium.WebDriverProvider;

import java.util.List;

import static org.junit.Assert.fail;

/**
 * User: vitaliy.vasilenko
 * Date: 7/2/13
 * Time: 6:31 PM
 */
public abstract class AbstractPage extends WebDriverPage {

    public AbstractPage(WebDriverProvider driverProvider) {
        super(driverProvider);
    }

    public void found(String text) {
        found(getPageSource(), text);
    }

    public void found(String pageSource, String text) {
        if (!pageSource.contains(escapeHtml(text))) {
            fail("Text: '" + text + "' not found in page '" + pageSource + "'");
        }
    }

    public void found(List<String> texts) {
        for (String text : texts) {
            found(text);
        }
    }

    private String escapeHtml(String text) {
        return text.replace("<", "&lt;").replace(">", "&gt;");
    }

}