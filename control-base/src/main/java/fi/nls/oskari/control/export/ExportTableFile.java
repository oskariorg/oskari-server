package fi.nls.oskari.control.export;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.CSVStreamer;
import fi.nls.oskari.util.TabularFileStreamer;
import fi.nls.oskari.util.XLSXStreamer;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by TMIKKOLAINEN on 30.12.2014.
 */
@OskariActionRoute("ExportTableFile")
public class ExportTableFile extends ActionHandler {
    private static final String CONTENT_TYPE_CSV = "application/CSV";
    private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXTENSION_CSV = "csv";
    private static final String EXTENSION_XLSX = "xlsx";
    private static final String FORMAT_CSV = "CSV";
    private static final String FORMAT_XLSX = "XLSX";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final HttpServletResponse response = params.getResponse();
        OutputStream out;
        try {
            out = response.getOutputStream();
        } catch (IOException ioe) {
            throw new ActionException(ioe.getMessage(), ioe);
        }
        TabularFileStreamer fileStreamer;
        String fileName = "export";
        String fileExtension = "";
        String format = params.getRequiredParam("format");
        String delimiter;
        String dataSource = params.getHttpParam("dataSource", null);
        String metadata = params.getHttpParam("metadata", null);
        String[] headers;
        Object[][] data;
        if (FORMAT_CSV.equals(format)) {
            fileStreamer = new CSVStreamer();
            delimiter = params.getHttpParam("delimiter", ",");
            if (delimiter.length() < 1) {
                throw new ActionException("Invalid delimiter:\"" + delimiter + "\"");
            }
            if ("tab".equals(delimiter)) {
                ((CSVStreamer) fileStreamer).setDelimiter('\t');
            } else {
                ((CSVStreamer) fileStreamer).setDelimiter(delimiter.charAt(0));
            }
            response.setContentType(CONTENT_TYPE_CSV);
            response.setCharacterEncoding("UTF-8");
            fileExtension = EXTENSION_CSV;
        } else if (FORMAT_XLSX.equals(format)) {
            fileStreamer = new XLSXStreamer();
            response.setContentType(CONTENT_TYPE_XLSX);
            fileExtension = EXTENSION_XLSX;
        } else {
            throw new ActionException("Unknown export format: \"" + format + "\"");
        }

        response.setHeader( "Content-Disposition", "attachment;filename=" + fileName + "." +  fileExtension);

        try {
            int rowIndex = 0;
            int cellIndex;
            int maxIndex;
            JSONArray dataArray = new JSONArray(params.getRequiredParam("data"));
            // Get headers
            JSONArray row = dataArray.getJSONArray(rowIndex);
            rowIndex++;
            headers = new String[row.length()];
            for (cellIndex = 0; cellIndex < headers.length; cellIndex++) {
                headers[cellIndex] = row.getString(cellIndex);
            }
            // Get data
            data = new Object[dataArray.length() - 1][headers.length];
            for (; rowIndex < dataArray.length(); rowIndex++) {
                row = dataArray.getJSONArray(rowIndex);
                maxIndex = Math.min(headers.length, row.length());
                for (cellIndex = 0; cellIndex < maxIndex; cellIndex++) {
                    Object tmp = row.get(cellIndex);
                    Object[] rowArr = data[rowIndex - 1];
                    rowArr[cellIndex] = tmp;
                }
            }
        } catch (JSONException je) {
            throw new ActionException(je.getMessage(), je);
        }
        Map<String, Object> additionalFields = new HashMap<String, Object>();
        if (dataSource != null) {
            additionalFields.put("Data source", dataSource);
        }
        if (metadata != null) {
            additionalFields.put("Metadata link", metadata);
        }

        try {
            fileStreamer.writeToStream(headers, data, additionalFields, out);
        } catch (IOException ioe) {
            throw new ActionException(ioe.getMessage(), ioe);
        }
    }
}
