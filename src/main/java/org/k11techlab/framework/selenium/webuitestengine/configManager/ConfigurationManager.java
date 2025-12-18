/******************************************************************************
 * Copyright 2025, K11 Software Solutions. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Kavita Jadhav (kavita.jadhav.sdet@gmail.com)
 ******************************************************************************/


package org.k11techlab.framework.selenium.webuitestengine.configManager;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

public class ConfigurationManager {
        // DEBUG: Print all values of 'packageName' from all loaded configurations
        public void debugPrintAllPackageNames() {
            if (configuration == null) {
                System.out.println("[DEBUG] CombinedConfiguration is null");
                return;
            }
            System.out.println("[DEBUG] All 'packageName' values from loaded configs:");
            for (int i = 0; i < configuration.getNumberOfConfigurations(); i++) {
                org.apache.commons.configuration2.Configuration conf = configuration.getConfiguration(i);
                String value = conf.getString("packageName", null);
                System.out.println("  Config " + i + ": " + value);
            }
            String effective = configuration.getString("packageName", null);
            System.out.println("[DEBUG] Effective 'packageName': " + effective);
        }
            // DEBUG: Print all values of 'mcp.port' from all loaded configurations
            public void debugPrintAllMcpPorts() {
                if (configuration == null) {
                    System.out.println("[DEBUG] CombinedConfiguration is null");
                    return;
                }
                System.out.println("[DEBUG] All 'mcp.port' values from loaded configs:");
                for (int i = 0; i < configuration.getNumberOfConfigurations(); i++) {
                    org.apache.commons.configuration2.Configuration conf = configuration.getConfiguration(i);
                    String value = conf.getString("mcp.port", null);
                    System.out.println("  Config " + i + ": " + value);
                }
                String effective = configuration.getString("mcp.port", null);
                System.out.println("[DEBUG] Effective 'mcp.port': " + effective);
            }
    private static final Log log = LogFactory.getLog(ConfigurationManager.class);
    private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    private final CombinedConfiguration configuration;
    private final PropertyUtil propertyUtil;

    private ConfigurationManager() {
        CombinedConfiguration config=
              ConfigurationLoader.loadConfigurationsFromFolder(
                        System.getProperty("user.dir") + File.separator +"config");
        propertyUtil = new PropertyUtil(config);
        this.configuration = config;
     }

     public static ConfigurationManager getInstance() {
        return INSTANCE;
    }

    public static PropertyUtil getBundle() {
        return getInstance().propertyUtil;
    }

    public static String getString(String key, String defaultString) {
        return INSTANCE.configuration.getString(key, defaultString);
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public int getInt(String key) {
        return configuration.getInt(key, 0);
    }

    // Additional getter methods for other data types
    public boolean getBoolean(String key) {
        return configuration.getBoolean(key, false);
    }

    public long getLong(String key, long defaultValue) {
        return configuration.getLong(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return configuration.getFloat(key, defaultValue);
    }

    public double getDouble(String key) {
        return configuration.getDouble(key, 0.0);
    }

    // Package-private method for testing purposes
    public static void setConfigurationForTesting(CombinedConfiguration config) {
        INSTANCE.configuration.addConfiguration(config);
    }
}
