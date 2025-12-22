package org.k11techlab.framework_unittests.ai_generated;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GeneratedPlaywrightLoginTest {

    private static Playwright playwright;
    private static Browser browser;
    private static Page page;

    @BeforeClass
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
    }

    @Test
    public void testLoginAndVerifyDashboard() {
        String webUrl = ConfigurationManager.getBundle().getPropertyValue("weburl");
        String username = ConfigurationManager.getBundle().getPropertyValue("username");
        String password = ConfigurationManager.getBundle().getPropertyValue("password");

        page.navigate(webUrl);

        if (page.isVisible("a:has-text('Dashboard')")) {
            page.click("a:has-text('Dashboard')");
        }

        page.fill("input[name='username']", username);
        page.fill("input[name='password']", password);
        page.click("button[type='submit']");

        assert page.isVisible("text=Dashboard");
    }

    @AfterClass
    public void tearDown() {
        playwright.close();
    }
}