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
 * To indicate automation error. Not an AUT failure so that the test case can be
 * in skip state instead of fail
 */
public class AutomationError extends SkipException {

    private static final long serialVersionUID = 2820870863950734300L;

    /**
     * Constructs a new runtime exception.
     *
     * @param msg The specified detail message
     */
    public AutomationError(String msg) {
        super(msg);
    }

    /**
     * Constructs a new runtime exception.
     *
     * @param msg The specified detail message
     * @param cause The cause
     */
    public AutomationError(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a new runtime exception.
     *
     * @param cause The cause
     */
    public AutomationError(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public final boolean isSkip() {
        return true;
    }
}
