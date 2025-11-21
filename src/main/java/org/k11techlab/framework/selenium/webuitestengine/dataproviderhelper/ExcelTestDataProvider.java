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

package org.k11techlab.framework.selenium.webuitestengine.dataproviderhelper;

import org.k11techlab.framework.selenium.webuitestengine.annotations.K11DataProvider;
import org.k11techlab.framework.selenium.webuitestengine.exceptions.DataProviderException;
import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.k11techlab.framework.selenium.webuitestbase.ApplicationProperties;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;

import java.lang.reflect.Method;

public class ExcelTestDataProvider {


    @DataProvider(name = "k11techlab-data-provider", parallel = true)
    public static Object[][] fetchExcelData(Method method){
        Log.debug("Initializing the test data");
        String dataPath= ApplicationProperties.TESTDATA_DIR.getStringVal();
        String dataFile = getDataFileNameFromAnnotation(method);
        return ExcelDataProviderHelper.convertExcelDataToObjectArray(dataPath+dataFile, "0");

    }

    public static String getDataFileNameFromAnnotation(Method method){
        String dataFile="";
        try {
            dataFile = method.getDeclaredAnnotation(K11DataProvider.class).dataFile();
        }catch(Exception e){
            throw new DataProviderException("The test data file name has not been specified. " +
                    "Please specify filename in the k11CustomDataProvider annotation.");
        }
        return dataFile;
    }

    public static String getParametersFromAnnotation(Method method){
        String parameter ="";
        try {
            parameter = method.getDeclaredAnnotation(Parameters.class).toString();
        }catch(Exception e){
            Log.info("No parameters found in the testNG 'parameters' annotation.");
        }
        return parameter;
    }

}
