package fi.nls.oskari.fe.input.jackson;

import java.io.IOException;

import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

public class FEFromXmlParser extends FromXmlParser {

    public FEFromXmlParser(IOContext ctxt, int genericParserFeatures,
            int xmlFeatures, ObjectCodec codec, XMLStreamReader xmlReader) {
        super(ctxt, genericParserFeatures, xmlFeatures, codec, xmlReader);
        // TODO Auto-generated constructor stub
    }

    public void resetContext() throws IOException {
        // _nextToken = null;
        // skipChildren();
        /*
         * _parsingContext = _parsingContext.getParent(); _namesToWrap =
         * _parsingContext.getNamesToWrap(); _xmlTokens.skipEndElement();
         */

    }
}
