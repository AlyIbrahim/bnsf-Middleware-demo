package com.example;

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

        rest("/").post("xml").consumes("application/xml").to("log: XML Train Data Received").to("direct://myservice");//.to("stream:out");

        from("direct://myservice").unmarshal(xmlDataFormat)
        //   .to("atlas:map/test.adm");
        .marshal(jsonDataFormat)
          //        .unmarshal(new JaxbDataFormat(JAXBContext.newInstance(ApplicationBatch.class))).
          .log("Train data Transformed to JSON ${body}")
          .to("file://mydata")
          .to("activemq:queue:traindata?disableReplyTo=true");

        from("jms:traindata").log("JSON Train Data Received from AMQ queue traindata ${body}");

        //   from("activemq:foo")
        //   .to("log:sample");

        //   from("timer:bar")
        //   .setBody(constant("Hello from Camel"))
        //   .to("activemq:foo");

    }

}
