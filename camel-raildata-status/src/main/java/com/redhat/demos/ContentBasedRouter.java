package com.redhat.demos;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class ContentBasedRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("amqp://raildata-json")

        .unmarshal().json().log("Rceived Raildata from Queue:  ${body}}")
        .choice()
        .when(simple("${body['status']}"))
            .log("Status is true, Insert to DB").to("sql: insert into RAILDATA(yardid, yardname, railcarid, railcartype, linkid, linkfromstation, linktostation) " +
            " Values(:#${body['yardid']}, :#${body['yardname']}, :#${body['railcarid']}, :#${body['railcartype']}, :#${body['linkid']}, :#${body['linkfromstation']}, :#${body['linktostation']})")
        .otherwise()
            .log("Status is false, Send to kafka")
            .to("kafka:raildata-status");
    }

}
