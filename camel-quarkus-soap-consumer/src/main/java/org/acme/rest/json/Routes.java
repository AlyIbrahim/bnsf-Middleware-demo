
package org.acme.rest.json;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

/**
 * Camel route definitions.
 */
public class Routes extends RouteBuilder {
    
    public Routes() {
 }

    @Override
    public void configure() throws Exception {

    JaxbDataFormat xmlDataFormat = new JaxbDataFormat();
    JAXBContext con = JAXBContext.newInstance(Train.class);
    xmlDataFormat.setContext(con);
    
    JacksonDataFormat jsonDataFormat = new JacksonDataFormat(Train.class);
    jsonDataFormat.setPrettyPrint(true);
        
    from("platform-http:/camel/xml?httpMethodRestrict=POST")
    .to("file://mydata").to("direct:myservice");
        from("direct:myservice").unmarshal(xmlDataFormat)
            .marshal(jsonDataFormat)
            .log("Train data Transformed to JSON ${body}")
            .to("kafka:traindata?brokers=kafka-service:9092");

            // .to("activemq:queue:traindata?disableReplyTo=true");

    }
}
