package com.redhat.demos;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
    public void configure() throws JAXBException {

        JaxbDataFormat xmlDataFormat = new JaxbDataFormat();
		JAXBContext con = JAXBContext.newInstance(Train.class);
        xmlDataFormat.setContext(con);
        
        JacksonDataFormat jsonDataFormat = new JacksonDataFormat(Train.class);
        jsonDataFormat.setPrettyPrint(true);
        
        from("activemq:traindata-xml").unmarshal(xmlDataFormat).marshal(jsonDataFormat)
        .log("Train data Transformed to JSON ${body}")
        .to("activemq:queue:traindata-json?disableReplyTo=true");
    }

}
