package org.k11techlab.framework_unittests.mcp_tests;

import com.microsoft.playwright.*;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

public class PlaywrightJavaLoginTest {
    @Test
    public void testLoginToDashboard() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            String webUrl = System.getenv().getOrDefault("WEBURL", "https://example.com");
            String username = System.getenv().getOrDefault("USERNAME", "testuser");
            String password = System.getenv().getOrDefault("PASSWORD", "testpass");
            page.navigate(webUrl);
            page.click("text=Login");
            page.fill("input[name='username']", username);
            page.fill("input[name='password']", password);
            page.click("text=Submit");
            boolean dashboardVisible = page.isVisible("text=Dashboard");
            assertTrue(dashboardVisible, "Dashboard should be visible after login");
            browser.close();
        }
    }
}
