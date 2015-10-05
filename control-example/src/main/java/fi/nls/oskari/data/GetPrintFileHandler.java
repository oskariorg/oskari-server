package fi.nls.oskari.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

/**
 * Get saved pdf or pgn file
 * parcel.pid is part parcel plot id
 */
@OskariActionRoute("parcel.getPrintFile")
public class GetPrintFileHandler extends ActionHandler {

    private final static String PARAM_PARCEL_PID = "parcel.pid";
    //private final static Logger log = LogFactory.getLogger(GetPrintFileHandler.class);
    private static String printSaveFilePath;
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_VALUE = "application/pdf";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HEADER_CONTENT_DISPOSITION_VALUE ="attachment; filename=";
    private static final String PDF_EXT =".pdf";

    @Override
    public void init() {

        printSaveFilePath = PropertyUtil.get("service.print.saveFilePath");

    }


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String partParcel_id = params.getRequiredParam(PARAM_PARCEL_PID);
        final HttpServletResponse response = params.getResponse();

        String filePath = printSaveFilePath + partParcel_id;
        if(filePath.indexOf(PDF_EXT) == -1) filePath = filePath + PDF_EXT;

        try {

            byte[] bytes = readBytes(filePath);


            response.addHeader(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE);
            response.addHeader(HEADER_CONTENT_DISPOSITION, HEADER_CONTENT_DISPOSITION_VALUE
                    + partParcel_id);

            response.getOutputStream().write(bytes, 0, bytes.length);
            response.getOutputStream().flush();
            response.getOutputStream().close();

        } catch (Exception e) {
            throw new ActionException("Couldn't get the file",
                    e);
        }
    }

    private byte[] readBytes(String filename) throws ActionException {
        try {
            FileInputStream is = new FileInputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int reads = is.read();

            while (reads != -1) {
                baos.write(reads);
                reads = is.read();
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new ActionException("Couldn't get the file",
                    e);
        }


    }
}