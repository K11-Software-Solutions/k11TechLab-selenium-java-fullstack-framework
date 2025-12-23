package org.k11techlab.framework_unittests.ai_generated.k11softwaresolutions;

import com.microsoft.playwright.Page;

public class LoginPage {
    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void enterUsername(String username) {
        String selector = null;
        if (page.querySelector("#username") != null) {
            selector = "#username";
        } else if (page.querySelector("input[name='username']") != null) {
            selector = "input[name='username']";
        } else if (page.querySelector("text=Username") != null) {
            selector = "text=Username";
        }
        if (selector != null) {
            page.fill(selector, username);
        }
    }

    public void enterPassword(String password) {
        String selector = null;
        if (page.querySelector("#password") != null) {
            selector = "#password";
        } else if (page.querySelector("input[name='password']") != null) {
            selector = "input[name='password']";
        } else if (page.querySelector("text=Password") != null) {
            selector = "text=Password";
        }
        if (selector != null) {
            page.fill(selector, password);
        }
    }

    public void clickLogin() {
        String selector = null;
        if (page.querySelector("#login") != null) {
            selector = "#login";
        } else if (page.querySelector("button[name='login']") != null) {
            selector = "button[name='login']";
        } else if (page.querySelector("text=Login") != null) {
            selector = "text=Login";
        } else if (page.querySelector("button[type='submit']") != null) {
            selector = "button[type='submit']";
        }
        if (selector != null) {
            page.click(selector);
        }
    }

    public boolean isDashboardVisible() {
        return page.isVisible("text=Dashboard");
    }
}
