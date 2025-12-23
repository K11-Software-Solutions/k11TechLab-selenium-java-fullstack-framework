package org.k11techlab.framework_unittests.playwright;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.k11techlab.framework.playwright.PlaywrightManager;
import org.k11techlab.framework.playwright.BasePlaywrightTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.aventstack.extentreports.ExtentTest;

public class PlaywrightManagerUnitTest extends BasePlaywrightTest {
    private ExtentTest test;

    @Test
    public void testSingletonInstance() {
        test = extent.createTest("testSingletonInstance");
        PlaywrightManager another = PlaywrightManager.getInstance();
        try {
            Assert.assertEquals(manager, another, "PlaywrightManager should be singleton");
            test.pass("Singleton instance verified");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    @Test
    public void testBrowserInitialization() {
        test = extent.createTest("testBrowserInitialization");
        Browser browser = manager.getBrowser();
        try {
            Assert.assertNotNull(browser, "Browser should be initialized");
            test.pass("Browser initialized");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    @Test
    public void testContextInitialization() {
        test = extent.createTest("testContextInitialization");
        BrowserContext context = manager.getContext();
        try {
            Assert.assertNotNull(context, "BrowserContext should be initialized");
            test.pass("Context initialized");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    @Test
    public void testPageInitialization() {
        test = extent.createTest("testPageInitialization");
        Page page = manager.getPage();
        try {
            Assert.assertNotNull(page, "Page should be initialized");
            test.pass("Page initialized");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    @Test
    public void testNewPage() {
        test = extent.createTest("testNewPage");
        Page newPage = manager.newPage();
        try {
            Assert.assertNotNull(newPage, "newPage() should return a new Page");
            test.pass("New page created");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
        newPage.close();
    }

    @Test
    public void testClosePage() {
        test = extent.createTest("testClosePage");
        Page page = manager.getPage();
        manager.closePage();
        try {
            java.lang.reflect.Field pageField = manager.getClass().getDeclaredField("page");
            pageField.setAccessible(true);
            Object internalPage = pageField.get(manager);
            Assert.assertNull(internalPage, "Page should be null after closePage()");
            test.pass("Page closed");
        } catch (Exception e) {
            test.fail(e);
            Assert.fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testCloseContext() {
        test = extent.createTest("testCloseContext");
        manager.closeContext();
        try {
            Assert.assertNull(manager.getContext(), "Context should be null after closeContext()");
            test.pass("Context closed");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    @Test
    public void testCloseBrowser() {
        test = extent.createTest("testCloseBrowser");
        manager.closeBrowser();
        try {
            Assert.assertNull(manager.getBrowser(), "Browser should be null after closeBrowser()");
            test.pass("Browser closed");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    @Test
    public void testQuit() {
        test = extent.createTest("testQuit");
        manager.quit();
        try {
            Assert.assertNull(manager.getBrowser(), "Browser should be null after quit()");
            Assert.assertNull(manager.getContext(), "Context should be null after quit()");
            test.pass("PlaywrightManager quit");
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    @Test
    public void testNavigateToWebUrl() {
        test = extent.createTest("testNavigateToWebUrl");
        String webUrl = org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager.getBundle().getPropertyValue("weburl");
        try {
            Assert.assertNotNull(webUrl, "weburl must be set in mcp-config.properties");
            Page page = manager.getPage();
            page.navigate(webUrl);
            String title = page.title();
            Assert.assertNotNull(title, "Page title should not be null after navigation");
            Assert.assertFalse(title.isEmpty(), "Page title should not be empty after navigation");
            test.pass("Navigation to weburl successful, title: " + title);
        } catch (AssertionError e) {
            test.fail(e);
            throw e;
        }
    }

    // Teardown is handled by BasePlaywrightTest
}
