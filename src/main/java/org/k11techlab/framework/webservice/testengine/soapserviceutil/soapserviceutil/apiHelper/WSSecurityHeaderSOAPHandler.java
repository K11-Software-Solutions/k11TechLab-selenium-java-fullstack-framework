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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;


public class WSSecurityHeaderSOAPHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String SOAP_ELEMENT_PASSWORD = "Password";
    private static final String SOAP_ELEMENT_USERNAME = "Username";
    private static final String SOAP_ELEMENT_USERNAME_TOKEN = "UsernameToken";
    private static final String SOAP_ELEMENT_SECURITY = "Security";
    private static final String NAMESPACE_SECURITY = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String PREFIX_SECURITY = "wsse";
    private String usernameText;
    private String passwordText;

    public WSSecurityHeaderSOAPHandler(String usernameText, String passwordText) {
        this.usernameText = usernameText;
        this.passwordText = passwordText;
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        Boolean outboundProperty = (Boolean) soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty.booleanValue()) {
            try {
                SOAPEnvelope soapEnvelope = soapMessageContext.getMessage().getSOAPPart().getEnvelope();
                SOAPHeader header = soapEnvelope.getHeader();
                if (header == null) {
                    header = soapEnvelope.addHeader();
                }
                SOAPElement soapElementSecurityHeader = header.addChildElement(SOAP_ELEMENT_SECURITY, PREFIX_SECURITY,
                        NAMESPACE_SECURITY);
                SOAPElement soapElementUsernameToken = soapElementSecurityHeader
                        .addChildElement(SOAP_ELEMENT_USERNAME_TOKEN, PREFIX_SECURITY);
                SOAPElement soapElementUsername = soapElementUsernameToken.addChildElement(SOAP_ELEMENT_USERNAME,
                        PREFIX_SECURITY);
                soapElementUsername.addTextNode(this.usernameText);
                SOAPElement soapElementPassword = soapElementUsernameToken.addChildElement(SOAP_ELEMENT_PASSWORD,
                        PREFIX_SECURITY);
                soapElementPassword.addTextNode(this.passwordText);
            } catch (Exception e) {
                throw new RuntimeException("Error on wsSecurityHandler: " + e.getMessage());
            }
        }
        return true;
    }
    @Override
    public void close(MessageContext context) {
// TODO Auto-generated method stub
    }
    @Override
    public boolean handleFault(SOAPMessageContext context) {
// TODO Auto-generated method stub
        return true;
    }
    @Override
    public Set<QName> getHeaders() {
// TODO Auto-generated method stub
        return null;
    }


}
