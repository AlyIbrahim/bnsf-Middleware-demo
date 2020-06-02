package com.redhat.demos;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class ContentBasedRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("jms://queue:raildata-json?connectionFactory=#pooledJmsConnectionFactory")
        .unmarshal().json(JsonLibrary.Jackson)
        .log(LoggingLevel.INFO, "Rceived Raildata from Queue:  ${body}}")
        .choice()
        .when(simple("${body['yardid']} > 100"))
            .log("Status is true, Insert to DB").to("sql: insert into RAILDATA(yardid, yardname, railcarid, railcartype, linkid, linkfromstation, linktostation) " +
            " Values(:#${body['yardid']}, :#${body['yardname']}, :#${body['railcarid']}, :#${body['railcartype']}, :#${body['linkid']}, :#${body['linkfromstation']}, :#${body['linktostation']})")
        .otherwise()
            .log("Status is false, Send to kafka")
            .to("kafka:raildata-status");
    }

}
