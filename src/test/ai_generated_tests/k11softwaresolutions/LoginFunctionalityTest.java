package org.k11techlab.framework_unittests.ai_generated;

import org.k11techlab.framework.selenium.webuitestengine.configManager.ConfigurationManager;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.k11techlab.framework.selenium.webuitestbase.BaseSeleniumTest;
import org.k11techlab.framework.ai.healing.AIElementHealer;
import org.k11techlab.framework.ai.llm.LLMInterface;
import org.k11techlab.framework.ai.openai.OpenAIClient;

public class LoginFunctionalityTest extends BaseSeleniumTest {

    @Test
    public void testLoginFunctionality() {
        LLMInterface llmProvider = new OpenAIClient();
        AIElementHealer elementHealer = new AIElementHealer(llmProvider, getDriver());

        getDriver().get(ConfigurationManager.getBundle().getPropertyValue("weburl"));

        elementHealer.findElement("Username input field").sendKeys(ConfigurationManager.getBundle().getPropertyValue("username"));
        elementHealer.findElement("Password input field").sendKeys(ConfigurationManager.getBundle().getPropertyValue("password"));
        elementHealer.findElement("Login button").click();

        Assert.assertTrue(elementHealer.findElement("Dashboard").isDisplayed(), "Dashboard should be visible after login");
    }
}
