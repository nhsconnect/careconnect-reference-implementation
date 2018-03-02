/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.careconnect.ri.extranet;


import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.gatewaylib.interceptor.GatewayCamelPostProcessor;
import uk.nhs.careconnect.ri.gatewaylib.interceptor.GatewayCamelProcessor;

import java.io.InputStream;


/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class CamelRouter extends RouteBuilder {

    @Autowired
    protected Environment env;

    @Value("${fhir.restserver.serverBase}")
    private String serverBase;

    @Override
    public void configure()
    {

        GatewayCamelProcessor camelProcessor = new GatewayCamelProcessor();

        GatewayCamelPostProcessor camelPostProcessor = new GatewayCamelPostProcessor();


        from("direct:FHIRPatient")
                .routeId("Extranet Patient")
                .to("direct:HAPIServer");

        from("direct:FHIREncounter")
                .routeId("Extranet Encounter")
                .to("direct:HAPIServer");

        from("direct:FHIRCapabilityStatement")
                .routeId("Extranet CapabilityStatement")
                .to("direct:HAPIServer");

        from("direct:HAPIServer")
                .routeId("INT FHIR Server")
                .process(camelProcessor)
                .to("log:uk.nhs.careconnect.extranet.start?level=INFO&showHeaders=true&showExchangeId=true")
                .to(serverBase)
                .process(camelPostProcessor)
                .to("log:uk.nhs.careconnect.extranet.complete?level=INFO&showHeaders=true&showExchangeId=true")
                .convertBodyTo(InputStream.class);

    }


}
