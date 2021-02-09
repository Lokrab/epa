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
import ca.uhn.hl7v2.model.v25.segment.MSH;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7DataFormat;
import org.apache.camel.component.netty.DefaultChannelHandlerFactory;
import org.apache.camel.component.netty.codec.DatagramPacketObjectDecoder;
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
/**
 * A simple {@link RouteBuilder}.
 */

@ApplicationScoped
public class Camel71Route extends RouteBuilder {
    String greeting = "Hello world2";

    // is thread safe?
    private static final Logger LOGGER = Logger.getLogger("Camel71Route");




    @Override
    public void configure() throws Exception {
        LOGGER.info("## called configure() of class " + this.getClass().getName());

        if(false) from("timer:foo?period={{timer.period}}").noAutoStartup()
                .setBody().constant(greeting)
                .to("file:outbox?fileName=${date:now:yyyy-MM-dd-HH-mm-ss}_${exchangeId}.txt")
                .to( log("hi").showExchangePattern(false).showBodyType(false) )
                .to( "log:hi2" );

        // enable Jackson json type converter
        //getContext().getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");
        // allow Jackson json to convert to pojo types also (by default jackson only converts to String and other simple types)
        //getContext().getGlobalOptions().put("CamelJacksonTypeConverterToPojo", "true");

//        //from object to JSON
//        Gson gson = new Gson();
//        gson.toJson(yourObject);
//
//        // from JSON to object
//        yourObject o = gson.fromJson(JSONString,yourObject.class);
        //{"name":"Some name","amount":1.0,"price":69.99}
        from("file://inbox").routeId("route3").noAutoStartup()
                .to( "log:before_process" )
                .to(log("hi").showExchangePattern(false).showBodyType(false))
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String body = exchange.getIn().getBody(String.class);
                        String json = body;



                        String s1 = JsonPath.read(json, "$.name");
                        LOGGER.info("##> JsonPath title is: " + s1);

//                        List<Map<String, Object>> books =  JsonPath.parse(json)
//                                .read("$.store.book[?(@.price < 10)]");
//                        LOGGER.info("##> books toString: " + books.toString());
                    }
                })
                ;
        from("file://inbox").routeId("route1").noAutoStartup()
            .to( "log:before_process" )
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
//            .to( "log:after_jsonpath" )
            .to("file:outbox?fileName=${date:now:yyyy-MM-dd-HH-mm-ss}_${exchangeId}.txt")
            .to( log("hi").showExchangePattern(false).showBodyType(false) )
            .to( "log:hi3" )
            ;
        from("file://inbox").routeId("toPACS").noAutoStartup()
                .to("log:before_process")
                .unmarshal().hl7() //to object. type: ca.uhn.hl7v2.model.v25.message.OMG_O19
                .log("after_unmarshal HL7Printer:print: ${bean:org.emias.HL7Printer?method=print}")
                .to( "log:after_unmarshal?showBody=true" )
                .process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                Object body = exchange.getIn().getBody();
                String msg= body.toString().replaceAll("\r","ZR");
                System.out.println(msg);
   /* String res1235="MSH|^~\\&|KIS|KIS.D|PACS|PACS.D|||OMG^O19|200410163300-000010|P|2.3.1|||||| ||\r"+
                "PID|||25003-20^^^KIS||BARHUDAROB ^Arov ^Grigorievich||19780324|F|||||||||||||||||||||||||||||||\r"+
                "PV1||||||||||||||||||||||||||||||||||||||||||||||||||||\r"+
                "ORC|NW|A50000Z^KIS_ORDPLC|||SC|||||||||||||AET1|\r"+
                "TQ1|||||||202004091510|||||\r"+
                "OBR|1|A50000Z^KIS_ORDPLC||804H_Code^804H_Code meaning^804H^804H_Code^804H_Code meaning^804H||||||||||||7101^FKIS^IKIS^P^^DR||ACC001|804H_Code|804H_Code||||CT||||||||||||||||||||804H_Code^804H_Code meaning^804H\r"+
                "ZDS|1.113654.3.13.1026^100^Application^DICOM\r";*/
                    //int port =2575;
                    int port =51172; // The port to listen on
                    boolean useTls = false; // Should we use TLS/SSL?
                    HapiContext context = new DefaultHapiContext();
                    /*HL7Service server = context.newServer(port, useTls);

                    ReceivingApplication<Message> handler = new ExampleReceiverApplication();
                    server.registerApplication("OMG", "019", handler);
*/

//                    server.registerConnectionListener(new SendAndReceiveAMessage.MyConnectionListener());
//
//                    server.setExceptionHandler(new SendAndReceiveAMessage.MyExceptionHandler());

                    // Start the server listening for messages
                    //server.startAndWait();


                    Parser p = context.getPipeParser();
                    Message adt = p.parse(msg);

                    // Remember, we created our HAPI Context above like so:
                    // HapiContext context = new DefaultHapiContext();

                    // A connection object represents a socket attached to an HL7 server
                    //Connection connection = context.newClient("10.10.0.109", port, useTls);
                Connection connection = context.newClient("localhost", port, useTls);
                    // The initiator is used to transmit unsolicited messages
                    Initiator initiator = connection.getInitiator();
                    Message response = initiator.sendAndReceive(adt);

                    String responseString = p.encode(response);
                    System.out.println("Received response:\n" + responseString);

                   /* connection = context.newClient("10.10.0.109", port, useTls);
                    initiator = connection.getInitiator();
                    response = initiator.sendAndReceive(adt);*/


                    connection.close();

                    // Stop the receiving server and client
                    //server.stopAndWait();
                 System.out.println("server is stopped");

        }})
        ;
        DataFormat hl7 = new HL7DataFormat();
        from("netty:tcp://localhost:51173?sync=true").noAutoStartup()
                .unmarshal(hl7).log("after_unmarshal HL7Printer:print: ${bean:org.emias.HL7Printer?method=print}")
                .process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                String body = exchange.getIn().getBody(String.class);
                exchange.getOut().setBody("Bye ");
                String answer ="MSH|^~\\&|PACS|PACS.D|KIS|KIS.D|20210209162832.147+0300||ACK^O19|1501|P|2.3.1\n" +
                        "MSA|AA|200410163300-000010";

            }
        });




        //System.getProperty("line.separator");


        // /r - 0d (LF), /n - 0a (CR), /r/n = newline(LFCR) = 0d0a
        //
        //https://camel.apache.org/components/latest/dataformats/hl7-dataformat.html
        //DataFormat hl7 = new HL7DataFormat();

        from("file://inbox"). routeId("route2").noAutoStartup()
                .to( "log:got_message" ) //message type is GenericFile
//                .process(new Processor() {
//                    public void process(Exchange exchange) throws Exception {
//                        //Object body = exchange.getIn().getBody();
//                        String body = exchange.getIn().getBody(String.class);
////                        String hl7msg = "MSH|^~\\&|*||HL7RCV1|DCM4CHEE|20180306223445.733||OMG^O19^OMG_O19|511814995|P|2.5||||||8859/1|||";
////                        hl7msg += "\rORC|SC|A100Z^^^MESA_ORDPLC|B100Z^^^MESA_ORDFIL||CM";
////                        hl7msg += "\rTQ1|||||||20180306223312.850";
////                        hl7msg += "\rOBR||A100Z^^^MESA_ORDPLC|B100Z^^^MESA_ORDFIL|||||||||||||||$ACCESSION_NUMBER$|$REQUESTED_PROCEDURE_ID$";
////                        exchange.getIn().setBody(hl7msg);
//                        LOGGER.info("##> body toString: " + body.replaceAll("\r","ZZR"));
//                    }
//                })
//                .to( "log:after_set_body" )
                //note: if convert ot string - \r makes console line not readableto("{{ftp.client}}")
                //.to("{{10.10.0.109:2575}}")
                .unmarshal().hl7() //to object. type: ca.uhn.hl7v2.model.v25.message.OMG_O19
                .log("after_unmarshal HL7Printer:print: ${bean:org.emias.HL7Printer?method=print}")
                .to( "log:after_unmarshal?showBody=true" )
                //.transform(hl7terser("OBR-2-1"))
                //.setHeader("OBR_18", hl7terser("/OBR-18")) //QRD-8(0)-1
                // ^ https://camel.apache.org/components/latest/languages/hl7terser-language.html
                // ^ https://hapifhir.github.io/hapi-hl7v2/xref/index.html
                //     navigate to ca.uhn.hl7v2.util.Terser
                // ^ https://sourceforge.net/p/hl7api/code/764/tree/trunk/hapi-mvn/hapi-base/src/main/java/ca/uhn/hl7v2/util/Terser.java
                //.to( "log:after_terser" )
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Object body = exchange.getIn().getBody();
                        //String body = exchange.getIn().getBody(String.class);
                        LOGGER.info("##> body toString: " + body.toString().replaceAll("\r","ZR"));

                        Message msg = exchange.getIn().getBody(Message.class);//начало
                        String bbb = body.toString().replaceAll("\r","ZR");

                        OMG_O19_ORDER omg_o19_order = (OMG_O19_ORDER)msg.get("ORDER"); //.get("OBSERVATION")
                        OMG_O19_OBSERVATION omg_o19_observation = (OMG_O19_OBSERVATION)omg_o19_order.get("OBSERVATION");
                        OBR obr = (OBR)omg_o19_order.get("OBR");
                        ST obr18 = obr.getObr18_PlacerField1();
                        EI obr2 = obr.getObr2_PlacerOrderNumber();
                        EI obr3 = obr.getObr3_FillerOrderNumber();
                        Type[] obr2Components = obr2.getComponents();
                        Type component0 = obr2Components[0];
                        String s1 = component0.toString();

                        String accessionNumber = obr18.toString();
                        //OBR qrd = (OBR)msg.get("OBR");
                        Object o  = msg.get("ORDER"); //OBR
                        //OMG_O19_ORDER = (OMG_O19_ORDER)o;
                        //next
                        // https://sourceforge.net/p/hl7api/code/764/tree/trunk/hapi-mvn/hapi-examples/src/main/java/ca/uhn/hl7v2/examples/ExampleUseTerser.java#l81
                        // https://sourceforge.net/p/hl7api/code/764/tree/trunk/hapi-mvn/hapi-base/src/main/java/ca/uhn/hl7v2/util/Terser.java#l52

                        //LOGGER.info("##> o toString: " + o.toString());
                        exchange.getIn().setBody(accessionNumber);
                    }
                })
                //at this step - body type is ca.uhn.hl7v2.model.v25.message.OMG_O19
                .to( "log:after_process?showBody=true" )
                //.marshal().hl7() //to byte[]
                //.to( "log:after_marshal?showBody=false" )
                .to("file:outbox?fileName=${date:now:yyyy-MM-dd-HH-mm-ss}_${exchangeId}.txt")
                .to( "log:hl7_done?showBody=false" );

    }
//    private static Message createADT01Message() throws Exception {
//        ADT_A01 adt = new ADT_A01();
//        adt.initQuickstart("ADT", "A01", "P");
//
//        // Populate the PID Segment
//        PID pid = adt.getPID();
//        pid.getPatientName(0).getFamilyName().getSurname().setValue("Doe");
//        pid.getPatientName(0).getGivenName().setValue("John");
//        pid.getPatientIdentifierList(0).getID().setValue(PATIENT_ID);
//
//        return adt;
//    }
//
public static class MyConnectionListener implements ConnectionListener {
    public void connectionReceived(Connection theC) {
        System.out.println("New connection received: " + theC.getRemoteAddress().toString());
    }

    public void connectionDiscarded(Connection theC) {
        System.out.println("Lost connection from: " + theC.getRemoteAddress().toString());
    }

}

    public static class MyExceptionHandler implements ReceivingApplicationExceptionHandler {

        public String processException(String theIncomingMessage, Map<String, Object> theIncomingMetadata, String theOutgoingMessage, Exception theE) throws HL7Exception {
                      /*
225           * Here you can do any processing you like. If you want to change
226           * the response (NAK) message which will be returned you may do
227           * so, or just return the NAK which HAPI already created (theOutgoingMessage)
228           */
            return theOutgoingMessage;
        }

    }
}

