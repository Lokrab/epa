package org.emias;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.group.OMG_O19_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.OMG_O19_ORDER;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationExceptionHandler;
import ca.uhn.hl7v2.protocol.impl.MLLPTransport;
import ca.uhn.hl7v2.util.Terser;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import org.apache.camel.BindToRegistry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7DataFormat;
import org.apache.camel.component.netty.DefaultChannelHandlerFactory;
import org.apache.camel.component.netty.codec.DatagramPacketObjectDecoder;
import org.apache.camel.language.bean.Bean;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spi.DataFormat;
import org.jboss.logging.Logger;
import org.apache.camel.component.hl7.HL7MLLPNettyDecoderFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;
import java.util.Map;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.log;
import static org.apache.camel.component.hl7.HL7.hl7terser;
import org.apache.camel.component.hl7.HL7MLLPNettyDecoderFactory;
import org.apache.camel.component.hl7.HL7MLLPNettyEncoderFactory;
import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.spi.DataFormat;

@ApplicationScoped
public class CamelListeningHL7Route extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger("Camellistening");


    @BindToRegistry("decoder")
    public ChannelHandler getDecoder() throws Exception {
        return new DefaultChannelHandlerFactory() {
            @Override
            public ChannelHandler newChannelHandler() {
                return new DatagramPacketObjectDecoder(ClassResolvers.weakCachingResolver(null));
            }
        };
    }



    @Override
    public void configure() throws Exception {

        LOGGER.info("## called configure() of class " + this.getClass().getName());
  /*      from("file://inbox").to( "log:before_process" )
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        //Object body = exchange.getIn().getBody();
//                    String body = exchange.getIn().getBody(String.class);
//                    String json = body;
//
////                    //Gson
////                    JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
////                    String name = jsonObject.get("name").toString();
////                    System.out.println("##> The name is: " + name);
////
//                    String s1 = JsonPath.read(json, "$.store.book[0].title");
//                    LOGGER.info("##> JsonPath title is: " + s1);
//
//                    List<Map<String, Object>> books =  JsonPath.parse(json)
//                            .read("$.store.book[?(@.price < 10)]");
//                    LOGGER.info("##> books toString: " + books.toString());
                    }
                })
//            .to( "log:before_jsonpath" )
//            .transform().jsonpath("$.name")
//            .to( "log:after_jsonpath"
                .to("file:outbox?fileName=${date:now:yyyy-MM-dd-HH-mm-ss}_${exchangeId}.txt")
                .to( log("hi").showExchangePattern(false).showBodyType(false) )
                .to( "log:hi3" )
        ;*/
       from("netty:tcp://localhost:51175?sync=true&decoders=#hl7decoder").log("sdfsd")//&decoders=#hl7decoder&encoders=#hl7encoder
                .unmarshal().hl7().log("after_unmarshal HL7Printer:print: ${bean:org.emias.HL7Printer?method=print}")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {

                    }
                }).marshal().hl7();
    }

}
