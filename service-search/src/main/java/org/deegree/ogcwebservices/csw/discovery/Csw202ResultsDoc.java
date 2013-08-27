package org.deegree.ogcwebservices.csw.discovery;

import java.util.Date;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.w3c.dom.Node;

public class Csw202ResultsDoc extends GetRecordsResultDocument {
    // private Log log = LogFactoryUtil.getLog(this.getClass());
        
    /**
     * 
     */
    private static final long serialVersionUID = -927482012333910311L;

    class MySearchStatus extends SearchStatus {
        MySearchStatus(String status, Date timestamp) {
            super(status, timestamp);
        }
    }

    public Csw202ResultsDoc() {
        super("2.0.2");
    }

    public GetRecordsResult parseGetRecordsResponse(GetRecords request)
        throws MissingParameterValueException,
               InvalidParameterValueException, OGCWebServiceException {
        try {
            // log.debug(XMLTools.getNamespaceForPrefix("csw", getRootElement()));
            // log.debug(getRootElement().toString());
            // log.debug(nsContext.getURI("csw"));
            // log.debug(nsContext.getURI("csw202"));
            String requestId = null;
            SearchStatus searchStatus = null;
            SearchResults searchResults = null;
            // '<csw:GetRecordsResponse>'-element (required)
            Node contextNode = XMLTools.getRequiredNode(getRootElement(),
                                                        "self::csw202:GetRecordsResponse", nsContext);

            getRootElement();
            // 'version'-attribute (optional)
            String version = XMLTools.getNodeAsString(contextNode, "@version",
                                                      nsContext, null);

            // log.debug(contextNode.getFirstChild().getNamespaceURI()
            //        + "/" + contextNode.getFirstChild().getLocalName());
            // '<CommonNamespaces.CSW202_PREFIX+":RequestId>'-element (optional)
            requestId = XMLTools.getNodeAsString(contextNode, "csw:RequestId",
                                                 nsContext, requestId);

            // '<CommonNamespaces.CSW202_PREFIX+":SearchStatus>'-element
            // (required)
            String status = XMLTools.getNodeAsString(contextNode, // Required
                                                     // vek
                                                     "csw202:SearchStatus/@status", nsContext, ""); // Geonetwork
            // ei
            // palauta
            String timestamp = XMLTools.getNodeAsString(contextNode,
                                                        "csw202:SearchStatus/@timestamp", nsContext, null);
            searchStatus = new SearchStatus(status, timestamp);

            // '<CommonNamespaces.CSW202_PREFIX+":SearchResults>'-element
            // (required)
            contextNode = XMLTools.getRequiredNode(contextNode,
                                                   "csw202:SearchResults", nsContext);

            // 'requestId'-attribute (optional)
            requestId = XMLTools.getNodeAsString(contextNode, "@requestId",
                                                 nsContext, requestId);

            // 'resultSetId'-attribute (optional)
            String resultSetId = XMLTools.getNodeAsString(contextNode,
                                                          "@resultSetId", nsContext, null);

            // 'elementSet'-attribute (optional)
            String elementSet = XMLTools.getNodeAsString(contextNode,
                                                         "@elementSet", nsContext, null);

            // 'recordSchema'-attribute (optional)
            String recordSchema = XMLTools.getNodeAsString(contextNode,
                                                           "@recordSchema", nsContext, null);

            // 'numberOfRecordsMatched'-attribute (required)
            int numberOfRecordsMatched = XMLTools.getRequiredNodeAsInt(
                                                                       contextNode, "@numberOfRecordsMatched", nsContext);

            // 'numberOfRecordsReturned'-attribute (required)
            int numberOfRecordsReturned = XMLTools.getRequiredNodeAsInt(
                                                                        contextNode, "@numberOfRecordsReturned", nsContext);

            // 'nextRecord'-attribute (required)
            int nextRecord = XMLTools.getRequiredNodeAsInt(contextNode,
                                                           "@nextRecord", nsContext);

            // 'expires'-attribute (optional)
            String expires = XMLTools.getNodeAsString(contextNode, "@expires",
                                                      nsContext, "null");

            searchResults = new SearchResults(requestId, resultSetId,
                                              elementSet, recordSchema, numberOfRecordsReturned,
                                              numberOfRecordsMatched, nextRecord, contextNode, expires);
            return new GetRecordsResult(request, version, searchStatus,
                                        searchResults);
        } catch (XMLParsingException e) {
            ExceptionCode code = ExceptionCode.INVALID_FORMAT;
            throw new OGCWebServiceException("GetRecordsResponseDocument",
                                             StringTools.stackTraceToString(e), code);
        } 
	// catch (URISyntaxException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        //     ExceptionCode code = ExceptionCode.INVALID_FORMAT;
        //     throw new OGCWebServiceException("GetRecordsResponseDocument",
        //                                      StringTools.stackTraceToString(e), code);
        // }

    }
}
