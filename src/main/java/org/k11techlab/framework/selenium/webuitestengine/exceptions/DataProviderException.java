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

package org.k11techlab.framework.selenium.webuitestengine.exceptions;

import org.testng.SkipException;

/**
 * exception class to indicate special skip
 *
 */
public class DataProviderException extends SkipException {

    public DataProviderException(String message) {
        super(message);

    }

    public DataProviderException(String message, Throwable cause) {
        super(message, cause);

    }

    @Override
    public boolean isSkip() {
        return true;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;
}
