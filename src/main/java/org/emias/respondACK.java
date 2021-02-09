package org.emias;
import ca.uhn.hl7v2.model.Message;
import org.apache.camel.Handler;
import org.apache.camel.spi.annotations.Component;

@Component("RouteBuilder")
public class respondACK {

    @Handler
    public Message process(Message in) throws Exception {
        System.out.println(in.toString());
        Message out =  in.generateACK();
        System.out.println(out.toString());
        return out;

    }
}