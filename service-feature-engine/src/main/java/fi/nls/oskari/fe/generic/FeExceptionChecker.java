package fi.nls.oskari.fe.generic;

/**
 * Checks, if gml/xml element is ExceptionText ( WFS 2.0.0 ) and
 * if yes --> TransportJobException is thrown
 * */


import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.wfs.WFSExceptionHelper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

public class FeExceptionChecker {

    private static final Logger log = LogFactory
            .getLogger(FeExceptionChecker.class);

    private static final String EXCEPTIONTEXT = "ExceptionText";

    /**
     * @param qn qualified xml element name
     * @return true, if element name is "ExceptionText"
     */
    public static boolean check(QName qn) {

        if (qn != null && EXCEPTIONTEXT.equals(qn.getLocalPart())) {
            //throw exception
            return true;
        }
        return false;
    }

    public static void breakAndThrow(XMLStreamReader xsr) {

        String message = "Exception in GetFeature request response";


        try {
            xsr.next();
        } catch (Exception e) {
            log.debug("*** Unknown next event", e);
        }

        if (xsr.getEventType() == XMLStreamReader.CHARACTERS)

        {
            if (xsr.hasText()) message = xsr.getText().trim();
        }
        log.debug(message);
        throw new ServiceRuntimeException(message,
                WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);


    }


}
