package org.k11techlab.framework_unittests.ai_generated;

import org.testng.annotations.Test;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.testng.Assert;
import com.microsoft.playwright.options.LoadState;

public class GeneratedTest_1765827236416Test {

    @Test
    public void testDashboardVisibility() {
        String webURL = System.getenv("WEBURL");
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.navigate(webURL);
            page.fill("input[name='username']", username);
            page.fill("input[name='password']", password);
            page.waitForLoadState(LoadState.NETWORKIDLE);
         

            Assert.assertTrue(page.isVisible("dashboardElement"));
            browser.close();
        }
        }
    }
