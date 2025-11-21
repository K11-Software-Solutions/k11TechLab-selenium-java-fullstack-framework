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

package org.k11techlab.framework.selenium.webuitestbase;

import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.k11techlab.framework.selenium.webuitestengine.dataproviderhelper.JsonDataProviderHelper.extractData_JSON;
import static org.k11techlab.framework.selenium.webuitestengine.dataproviderhelper.JsonDataProviderHelper.filterTestData;

/**
 * TestNG JSON DataProvider Utility Class
 *
 */
public class JSONDataProvider {

    public static String testCaseName = "NA";

    public JSONDataProvider() {
    }

    /**
     * fetchData method to retrieve test data for specified method
     *
     * @param method
     * @param datafileName
     * @return Object[][]
     */
    @DataProvider(name = "fetchData_JSON")
    public static Object[][] fetchData(Method method, String datafileName) {
        String rowID;
        Object[][] result;
        String dataPath = ApplicationProperties.TESTDATA_DIR.getStringVal();
        testCaseName = method.getName();
        List<JSONObject> testDataList = new ArrayList<>();
        JSONArray testData = (JSONArray) extractData_JSON(dataPath + datafileName).get(testCaseName);
        for (int i = 0; i < testData.size(); i++) {
            testDataList.add((JSONObject) testData.get(i));
        }

        // reassign testRows after filtering tests
        testDataList = filterTestData(testDataList);
        Log.LOGGER.info("The number of test cases to be executed is: " + testDataList.size());

        // create object for dataprovider to return
        try {
            result = new Object[testDataList.size()][testDataList.get(0).size()];
            for (int i = 0; i < testDataList.size(); i++) {
                rowID = testDataList.get(i).get("rowID").toString();
                result[i] = new Object[]{rowID, testDataList.get(i)};
            }
        } catch (IndexOutOfBoundsException ie) {
            Log.LOGGER.info(ie.getMessage());
            result = new Object[0][0]; // TODO does this needs to throw to tell us this is a defect ...
        }
        return result;
    }

}
