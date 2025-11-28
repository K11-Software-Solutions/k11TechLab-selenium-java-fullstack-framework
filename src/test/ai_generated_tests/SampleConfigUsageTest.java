package ai_generated_tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.FileInputStream;
import java.util.Properties;

public class SampleConfigUsageTest {
    @Test
    public void testAIProviderOrderConfig() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("config/chatbot.ai.properties"));
        String providerOrder = props.getProperty("ai.provider.priority");
        Assert.assertNotNull(providerOrder);
        Assert.assertTrue(providerOrder.contains("OPENAI"));
    }
}

