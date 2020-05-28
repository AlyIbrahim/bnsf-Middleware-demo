package com.redhat.demos;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(MySpringBootRouter.class);
    @Override
    public void configure() {
        from("amqp://traindata-json")
        .to("kafka:traindara-status");
    }

}
