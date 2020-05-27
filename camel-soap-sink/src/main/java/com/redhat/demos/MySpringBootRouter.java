package com.redhat.demos;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    @Override
    public void configure() {
        rest("/").post("xml").consumes("application/xml").to("log: XML Train Data Received").to("direct://myservice");
        from("direct://myservice").to("log: XML Train Data Received").to("amqp://traindata-xml?disableReplyTo=true");
        //to("jms://traindata-xml?disableReplyTo=true");
        //.to("activemq:traindata-xml?disableReplyTo=true");
    }

}
