package com.redhat.demos;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;


@Component
public class TransformationRouter extends RouteBuilder {

    @Override
    public void configure() {

        from("jms://queue:raildata-xml?connectionFactory=#pooledJmsConnectionFactory")
        .unmarshal().jaxb("com.redhat.demos")
        .marshal().json(JsonLibrary.Jackson, true)
        .log(LoggingLevel.INFO, "Rail data Transformed to JSON ${body}")
        .to(ExchangePattern.InOnly, "jms://queue:raildata-json?connectionFactory=#pooledJmsConnectionFactory");
    }

}
