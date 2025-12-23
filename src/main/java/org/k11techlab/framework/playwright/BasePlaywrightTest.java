package org.k11techlab.framework.playwright;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public abstract class BasePlaywrightTest {
    protected PlaywrightManager manager;
    protected static ExtentReports extent;

    @BeforeClass
    public void setUpBase() {
        manager = PlaywrightManager.getInstance();
        manager.setBrowserOptions(true, 0); // headless, no slowMo
        manager.initializeBrowser();
        if (extent == null) {
            extent = new ExtentReports();
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/PlaywrightManagerUnitTest-ExtentReport.html");
            try {
                spark.loadXMLConfig("config/extent-config.xml");
            } catch (java.io.IOException e) {
                e.printStackTrace();
                // Optionally, you can log or rethrow the exception as needed
            }
            extent.attachReporter(spark);
        }
    }

    @AfterClass
    public void tearDownBase() {
        manager.quit();
        if (extent != null) {
            extent.flush();
        }
    }
}
