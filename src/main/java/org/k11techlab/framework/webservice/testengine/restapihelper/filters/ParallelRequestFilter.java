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

package org.k11techlab.framework.webservice.testengine.restapihelper.filters;

import org.k11techlab.framework.selenium.webuitestengine.logger.Log;
import org.k11techlab.framework.webservice.apitestbase.BaseWebServiceTest;
import io.restassured.builder.ResponseBuilder;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.filter.log.LogDetail;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.internal.print.RequestPrinter;
import io.restassured.internal.print.ResponsePrinter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.hamcrest.Matchers;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Set;

/**
 * Makes Rest Assured parallel friendly.
 */
public class ParallelRequestFilter implements OrderedFilter {

    public static final ThreadLocal<HashMap<String, Response>> THREAD_RESPONSE = ThreadLocal.withInitial(HashMap::new);
    private boolean log;

    public ParallelRequestFilter(boolean log)
    {
        this.log = log;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {

        long currentThreadId = Thread.currentThread().getId();
        String methodName = BaseWebServiceTest.METHOD_THREAD.get().get(currentThreadId);
        Log.LOGGER.info(MessageFormat.format("Filter Request for threadId {0}",currentThreadId));

        logRequest(requestSpec);

        Response response = ctx.next(requestSpec, responseSpec);
        THREAD_RESPONSE.get().put(methodName, response);

        logResponse(methodName);
        Log.LOGGER.info(MessageFormat.format("Filter Request DONE for threadId = {0} and method {1}", currentThreadId, methodName));
        return THREAD_RESPONSE.get().get(methodName);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * Prints the Request is log is true.
     */
    private void logRequest(FilterableRequestSpecification requestSpec)
    {
        if(!log) {
            return;
        }

        Log.LOGGER.info("Logging Request");
        String loggedMessage = RequestPrinter.print(
                requestSpec,
                requestSpec.getMethod(),
                requestSpec.getURI(),
                LogDetail.ALL,
                Set.of("Authorization", "Cookie"),              // shouldPrettyPrint
                System.out,
                false              // urlEncoded — set to true/false based on your request
        );
        Log.info(MessageFormat.format("Web Service Request : {0}" ,loggedMessage), true);
        Log.LOGGER.info("Logging Request Done");
    }

    /**
     * Prints response.
     * @param methodName the method name
     * @return
     */
    private Response logResponse(String methodName)
    {

        Response response = THREAD_RESPONSE.get().get(methodName);
        if(!log) {
            return response;
        }

        final int statusCode = response.statusCode();
        String responseMessage = "";
        if (Matchers.any(Integer.class).matches(statusCode)) {

            responseMessage = ResponsePrinter.print(
                    response,
                    null,                       // ResponseSpecification (or provide one if needed)
                    System.out,                 // Output stream
                    LogDetail.ALL,              // What to log
                    true,                       // Pretty print
                    Set.of("Set-Cookie")        // 🔒 Headers to blacklist in logs
            );

            final byte[] responseBody = response.asByteArray();
            response = cloneResponseIfNeeded(response, responseBody);
        }
        Log.info(MessageFormat.format("Web Service Response : {0}", responseMessage), true);
        return response;
    }

    /*
     * If body expectations are defined we need to return a new Response otherwise the stream
     * has been closed due to the logging.
     */
    private Response cloneResponseIfNeeded(Response response, byte[] responseAsString) {
        Log.LOGGER.info("Cloning Response");
        if (responseAsString != null && response instanceof RestAssuredResponseImpl && !((RestAssuredResponseImpl) response).getHasExpectations()) {
            final Response build = new ResponseBuilder().clone(response).setBody(responseAsString).build();
            ((RestAssuredResponseImpl) build).setHasExpectations(true);
            return build;
        }
        Log.LOGGER.info("Cloning Response Done");
        return response;
    }
}
