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

package org.k11techlab.framework.webservice.testengine.soapserviceutil.soapserviceutil.apiHelper;

import org.k11techlab.framework.selenium.webuitestengine.commonUtil.LoadProperties;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;
 
public class JaxWsHandlerResolver implements HandlerResolver {
 
    @SuppressWarnings("rawtypes")
    @Override
    public List<Handler> getHandlerChain(PortInfo arg0) {
        List<Handler> hchain = new ArrayList<Handler>();
        LoadProperties prop= new LoadProperties();
        String username = prop.getProperty("service.username");
        String password = prop.getProperty("service.password");
        hchain.add(new JaxWsLoggingHandler());
        hchain.add(new WSSecurityHeaderSOAPHandler(username, password));
        return hchain;
    }
 
}
