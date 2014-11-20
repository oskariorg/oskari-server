package fi.nls.oskari.fe.input.jackson;

import java.io.IOException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

public class FEXmlFactory extends XmlFactory {

    public FromXmlParser createParser(XMLStreamReader sr) throws IOException {
        // note: should NOT move parser if already pointing to START_ELEMENT
        if (sr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            try {
                sr = _initializeXmlReader(sr);
            } catch (XMLStreamException e) {
                return StaxUtil.throwXmlAsIOException(e);
            }
        }

        // false -> not managed
        FromXmlParser xp = new FEFromXmlParser(_createContext(sr, false),
                _generatorFeatures, _xmlGeneratorFeatures, _objectCodec, sr);
        if (_cfgNameForTextElement != null) {
            xp.setXMLTextElementName(_cfgNameForTextElement);
        }
        return xp;
    }

}