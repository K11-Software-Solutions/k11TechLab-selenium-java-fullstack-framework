package org.k11techlab.framework.ai.rag.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DotenvLoader {
    public static void loadEnv(String envFilePath) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(envFilePath)) {
            props.load(fis);
            for (String key : props.stringPropertyNames()) {
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    // Set as system property for this JVM
                    System.setProperty(key, props.getProperty(key));
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load .env file: " + envFilePath);
        }
    }
}
