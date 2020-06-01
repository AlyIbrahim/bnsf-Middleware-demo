package com.example;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.dataformat.soap.SoapJaxbDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
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

        JaxbDataFormat xmlRailDataFormat = new JaxbDataFormat();
		JAXBContext context = JAXBContext.newInstance(RailData.class);
        Map<String, String> namespacePrefix = new HashMap<String,String>();
        // namespacePrefix.put("rail", "http://examples.bnsf.com/railyard.xsd");
        xmlRailDataFormat.setNamespacePrefix(namespacePrefix);
        xmlRailDataFormat.setContext(context);
        // xmlRailDataFormat.setSchema(schema);

        JacksonDataFormat jsonRailDataFormat = new JacksonDataFormat(RailData.class);
        jsonRailDataFormat.setPrettyPrint(true);
        // jsonRailDataFormat.setUnmarshalType(RailData.class);

        // rest("/").post("xml").consumes("application/xml").to("log: XML Train Data Received").to("direct://myservice");//.to("stream:out");
        rest("/").post("xml").consumes("application/xml").to("log: XML Train Data Received").to("direct://raildataservice");

        // from("direct://myservice").unmarshal(xmlDataFormat)
        // //   .to("atlas:map/test.adm");
        // .marshal(jsonDataFormat)
        //   //        .unmarshal(new JaxbDataFormat(JAXBContext.newInstance(ApplicationBatch.class))).
        //   .log("Train data Transformed to JSON ${body}")
        //   .to("file://mydata")
        //   .to("activemq:queue:traindata?disableReplyTo=true");

          from("direct://raildataservice")
          .unmarshal(xmlRailDataFormat)
          .marshal(jsonRailDataFormat)
          .log("Train data Transformed to JSON ${body}")
          .to("file://mydata")
          .to("activemq:queue:traindata?disableReplyTo=true");


        from("jms:traindata")
        .unmarshal().json().log("OOPPAA ${body['status']}")
        //.json(JsonLibrary.Jackson, RailData.class).log("OOPPAA ${body.status}")
        .choice()
        // .when(jsonpath("$.[?(@.yardid > 30)]"))
        // .when(jsonpath(".status"))
        .when(simple("${body['status']}"))
        // .when(simple("${body.status}"))
            .log("First Choice")
            .to("sql: insert into RAILDATA(yardid, yardname, railcarid, railcartype, linkid, linkfromstation, linktostation) " +
            " Values(:#${body['yardid']}, :#${body['yardname']}, :#${body['railcarid']}, :#${body['railcartype']}, :#${body['linkid']}, :#${body['linkfromstation']}, :#${body['linktostation']})")
        .otherwise()
            .log("Second Choice");

        // from("jms:traindata").log("JSON Train Data Received from AMQ queue traindata ${body}");

        //   from("activemq:foo")
        //   .to("log:sample");

        //   from("timer:bar")
        //   .setBody(constant("Hello from Camel"))
        //   .to("activemq:foo");

    }

}
