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

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.k11techlab.framework.selenium.webuitestbase.ApplicationProperties;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.k11techlab.framework.selenium.webuitestengine.dataproviderhelper.ExcelDataProviderHelper.convertToObjectArray;

public class CSVTestDataProvider {

  
    public static List<Map<?, ?>> readObjectsFromCsv(File file) throws IOException {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(bootstrap).readValues(file);

        return mappingIterator.readAll();
    }

    public static void writeAsJson(List<Map<?, ?>> data, File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(file, data);
    }

      public static void main(String[] args) throws Exception {
        String dataPath= ApplicationProperties.TESTDATA_DIR.getStringVal();
        File input = new File(dataPath+"filename.csv");
        File output = new File(dataPath+"filename.json");

        List<Map<?, ?>> data = readObjectsFromCsv(input);
        writeAsJson(data, output);
        Object[][] csvDataRowObjects= convertToObjectArray(data);
        System.out.println(csvDataRowObjects[0][0].toString().replaceAll(", =", ""));
        System.out.println(csvDataRowObjects[1][0].toString().replaceAll(", =", ""));

    }
}
