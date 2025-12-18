package org.k11techlab.framework.ai.mcp.workflow.selenium;

import org.k11techlab.framework.ai.util.DynamicSeleniumTestWorkflow;
import org.k11techlab.framework.ai.util.OpenAISeleniumTestGenerator;

/**
 * Thin wrapper around existing DynamicSeleniumTestWorkflow
 */
public class SeleniumWorkflow {

    private final DynamicSeleniumTestWorkflow delegate;

    public SeleniumWorkflow(OpenAISeleniumTestGenerator generator) {
        this.delegate = new DynamicSeleniumTestWorkflow(generator);
    }

    public String generateAndRunTest(
            String scenario,
            String className,
            String packageName) throws Exception {

        return delegate.generateAndRunTest(scenario, className, packageName);
    }
}
