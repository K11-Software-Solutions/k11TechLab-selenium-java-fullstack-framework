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

package org.k11techlab.framework.selenium.webuitestengine.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ METHOD, TYPE })
public @interface K11DataProvider {
	/**
	 * Represents meta-data for data provider
	 */
	public enum params {
		DATAFILE, SHEETNAME, KEY, DATAPROVIDER, DATAPROVIDERCLASS, FILTER, FROM, TO, INDICES;
	}

	public static final String NAME= "k11-data-provider";
	public static final String NAME_PARALLEL = "k11-data-provider-parallel";

	/**
	 * Used to provide csv or excel file. 
	 * 
	 * @return
	 */
	String dataFile() default "";

	/**
	 * Optional sheet name (value or property) for excel file. If not provided
	 * first sheet will be considered. 
	 *
	 * @return
	 */
	String sheetName() default "";

	/**
	 * Optional flag to indicate excel data contains header row that need to be
	 * skipped. Default value is false.
	 *
	 * @return
	 */
	boolean hasHeaderRow() default false;

	/***
	 * Optional data label name in excel sheet. Required if want to provide data
	 * start/end cell marked with label.
	 *
	 * @return
	 */
	String key() default "";

	String filter() default "";
}
