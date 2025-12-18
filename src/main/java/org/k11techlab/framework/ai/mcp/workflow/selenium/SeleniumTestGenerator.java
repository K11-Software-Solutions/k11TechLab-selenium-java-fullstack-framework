package org.k11techlab.framework.ai.mcp.workflow.selenium;

import org.k11techlab.framework.ai.util.OpenAISeleniumTestGenerator;

public class SeleniumTestGenerator {

    private final OpenAISeleniumTestGenerator delegate;

    public SeleniumTestGenerator(OpenAISeleniumTestGenerator delegate) {
        this.delegate = delegate;
    }

    public String generateTest(String scenario) {
        return delegate.generateTest(scenario);
    }
}


