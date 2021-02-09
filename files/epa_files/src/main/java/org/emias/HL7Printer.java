package org.emias;

import org.apache.camel.Body;

public class HL7Printer
{
    String print(@Body String body){
        return body.replaceAll("\r","<R>");
    }
}
