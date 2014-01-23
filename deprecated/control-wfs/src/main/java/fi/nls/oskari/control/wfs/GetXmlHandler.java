package fi.nls.oskari.control.wfs;

import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;

import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;

@OskariActionRoute("GET_XML_DATA")
public class GetXmlHandler extends FlowModelHandler {


    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        try {

            final String actionKey = params.getRequest().getParameter(KEY_ACTION);

            final FlowModel flowModel = createFlowModelFlowModel(params);
            // FIXME: tear down this internal processActions mapping
            processActions(actionKey, flowModel);
            
            final HttpServletResponse response = params.getResponse();            
            // Cache for hour 
            response.setHeader("Cache-Control", "must-revalidate, max-age=3600");
            response.setContentType("text/xml");
            final OutputStream os = response.getOutputStream();
            final String xmlData = flowModel.getAsString(KEY_XMLDATA);
            os.write(xmlData.getBytes());
            os.close();
        } catch (Exception e) {
            // Do not let real cause to be shown to end user
            throw new ActionException("Failed to create XML", e);
        }
    }
}
