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
        xmlDataFormat.setPrettyPrint(true);
        
        JacksonDataFormat jsonDataFormat = new JacksonDataFormat(Train.class);
        jsonDataFormat.setPrettyPrint(true);
        
        from("amqp://traindata-xml")
        .unmarshal(xmlDataFormat)
        // .to("log:Train Data from Queue ${body")
        .marshal(jsonDataFormat)
        .to("log:Train data Transformed to JSON ${body}")
        .to("amqp://queue:traindata-json?disableReplyTo=true");
    }

}
