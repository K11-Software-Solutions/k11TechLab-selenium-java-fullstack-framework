package org.k11techlab.framework.playwright;

import com.microsoft.playwright.*;

/**
 * Playwright-based Browser Manager
 * Manages Playwright browser instances and pages
 */
public class PlaywrightManager {
    private static volatile PlaywrightManager instance;
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    
    private boolean headless = false;
    private int slowMo = 50;
    
    private PlaywrightManager() {
        // Private constructor for singleton
    }
    
    public static PlaywrightManager getInstance() {
        if (instance == null) {
            synchronized (PlaywrightManager.class) {
                if (instance == null) {
                    instance = new PlaywrightManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize Playwright with specified browser
     * @param browserType Browser type (chromium, firefox, webkit)
     */
    public void initializeBrowser(String browserType) {
        quit(); // Ensure previous instances are closed
        playwright = Playwright.create();

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(headless)
            .setSlowMo(slowMo);

        switch (browserType.toLowerCase()) {
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "webkit":
            case "safari":
                browser = playwright.webkit().launch(launchOptions);
                break;
            case "chromium":
            case "chrome":
            default:
                browser = playwright.chromium().launch(launchOptions);
                break;
        }

        context = browser.newContext(new Browser.NewContextOptions()
            .setViewportSize(1920, 1080));
        page = context.newPage();
    }
    
    /**
     * Initialize Playwright with default Chromium browser
     */
    public void initializeBrowser() {
        initializeBrowser("chromium");
    }
    
    /**
     * Get the current page instance
     * @return Playwright Page
     */
    public Page getPage() {
        if (page == null) {
            initializeBrowser();
        }
        return page;
    }
    
    /**
     * Get the current browser instance
     * @return Playwright Browser
     */
    public Browser getBrowser() {
        return browser;
    }
    
    /**
     * Get the current browser context
     * @return Playwright BrowserContext
     */
    public BrowserContext getContext() {
        return context;
    }
    
    /**
     * Create a new page in the current context
     * @return New Playwright Page
     */
    public Page newPage() {
        if (context == null) {
            initializeBrowser();
        }
        return context.newPage();
    }
    
    /**
     * Close the current page
     */
    public void closePage() {
        if (page != null) {
            page.close();
            page = null;
        }
    }
    
    /**
     * Close the browser context
     */
    public void closeContext() {
        if (page != null) {
            try { page.close(); } catch (Exception ignored) {}
            page = null;
        }
        if (context != null) {
            try { context.close(); } catch (Exception ignored) {}
            context = null;
        }
    }
    
    /**
     * Close the browser
     */
    public void closeBrowser() {
        closeContext();
        if (browser != null) {
            try { browser.close(); } catch (Exception ignored) {}
            browser = null;
        }
    }
    
    /**
     * Quit Playwright completely
     */
    public void quit() {
        closeBrowser();
        if (playwright != null) {
            try { playwright.close(); } catch (Exception ignored) {}
            playwright = null;
        }
        // Do not nullify instance for singleton
    }
    
    /**
     * Set browser options
     * @param headless Whether to run in headless mode
     * @param slowMo Slow down operations by specified milliseconds
     */
    public void setBrowserOptions(boolean headless, int slowMo) {
        this.headless = headless;
        this.slowMo = slowMo;
    }
}