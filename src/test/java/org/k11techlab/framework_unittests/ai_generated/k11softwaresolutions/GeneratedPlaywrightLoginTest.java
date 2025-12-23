package org.k11techlab.framework_unittests.ai_generated.k11softwaresolutions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;

public class GeneratedPlaywrightLoginTest {

    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeClass
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        page = browser.newPage();
    }

    @Test
    public void testLoginAndVerifyDashboard() {
        String weburl = ConfigurationManager.getBundle().getPropertyValue("weburl");
        String username = ConfigurationManager.getBundle().getPropertyValue("username");
        String password = ConfigurationManager.getBundle().getPropertyValue("password");

        page.navigate(weburl);

        // Username field: id > name > text
        String usernameSelector = null;
        if (page.querySelector("#username") != null) {
            usernameSelector = "#username";
        } else if (page.querySelector("input[name='username']") != null) {
            usernameSelector = "input[name='username']";
        } else if (page.querySelector("text=Username") != null) {
            usernameSelector = "text=Username";
        }
        if (usernameSelector != null) {
            page.fill(usernameSelector, username);
        }

        // Password field: id > name > text
        String passwordSelector = null;
        if (page.querySelector("#password") != null) {
            passwordSelector = "#password";
        } else if (page.querySelector("input[name='password']") != null) {
            passwordSelector = "input[name='password']";
        } else if (page.querySelector("text=Password") != null) {
            passwordSelector = "text=Password";
        }
        if (passwordSelector != null) {
            page.fill(passwordSelector, password);
        }

        // Login button: id > name > text > type=submit
        String loginBtnSelector = null;
        if (page.querySelector("#login") != null) {
            loginBtnSelector = "#login";
        } else if (page.querySelector("button[name='login']") != null) {
            loginBtnSelector = "button[name='login']";
        } else if (page.querySelector("text=Login") != null) {
            loginBtnSelector = "text=Login";
        } else if (page.querySelector("button[type='submit']") != null) {
            loginBtnSelector = "button[type='submit']";
        }
        if (loginBtnSelector != null) {
            page.click(loginBtnSelector);
        }

        // Wait for dashboard text
        page.waitForSelector("text=Dashboard", new Page.WaitForSelectorOptions().setTimeout(5000));
        assert page.isVisible("text=Dashboard");
    }

    @AfterClass
    public void tearDown() {
        browser.close();
        playwright.close();
    }
}