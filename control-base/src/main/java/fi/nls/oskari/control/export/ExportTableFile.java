package fi.nls.oskari.control.export;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private static final Logger log = LogFactory.getLogger(ExportTableFile.class);

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
        String fileExtension;
        String format = params.getRequiredParam("format");
        String delimiter;
        String fileName = params.getHttpParam("filename", "export").replaceAll("[^a-zA-Z0-9.-]", "_");
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
            // Reform, if objects arrays and identical structure
            dataArray = reformIfIdenticalObjects(dataArray);
            // if json objects in columns
            if (hasObjectColumns(dataArray)){
                // try to expand object columns
                dataArray = expandObjectValues(dataArray);
            }
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
        // write additionalData (JSON array) after the actual data
        JSONArray additionalData = JSONHelper.createJSONArray(params.getHttpParam("additionalData", "[]"));
        // Using LinkedHashMap to control the order of items
        Map<String, Object> additionalFields = new LinkedHashMap<String, Object>();

        for (int i = 0; i < additionalData.length(); i++) {
            JSONObject item = additionalData.optJSONObject(i);
            if(item == null) {
                continue;
            }
            final String type = item.optString("type");
            String value = item.optString("value");
            if("metadata".equalsIgnoreCase(type)) {
                // metadata only holds uuid, add the rest of the url to the value
                value = getMetadataUrl(value);
            }
            additionalFields.put(item.optString("name"), value);
        }

        try {
            fileStreamer.writeToStream(headers, data, additionalFields, out);
        } catch (IOException ioe) {
            throw new ActionException(ioe.getMessage(), ioe);
        }
    }

    /**
     * reform data, if objects arrays and identical structure
     * Data must be in one column and column values are json objects
     *
     * @param data raw export data  (<Array>/<Array>)
     * @return
     */
    private JSONArray reformIfIdenticalObjects(JSONArray data) {
        if (data == null) return data;
        int colnum = 0;
        JSONArray dataArray = new JSONArray();
        try {
            JSONArray row = data.getJSONArray(0);  // header row
            if (row == null) return data;
            if (row.length() != 1) return data;

            for (int rowIndex = 1; rowIndex < data.length(); rowIndex++) {
                row = data.getJSONArray(rowIndex);
                if (row == null) return data;
                if (row.length() != 1) return data;
                JSONArray header = new JSONArray();
                JSONArray newdata = new JSONArray();
                if (row.get(0) instanceof JSONObject) {
                    Iterator<?> keys = row.getJSONObject(0).keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        // Set new header row
                        if (rowIndex == 1) {
                            header.put(key);
                        }
                        newdata.put(row.getJSONObject(0).get(key).toString());
                    }
                    if (rowIndex == 1) {
                        dataArray.put(header);
                        colnum = header.length();
                    }
                    dataArray.put(newdata);
                    if (newdata.length() != colnum) return data;  // must be identical row structure
                } else {
                    return data;
                }

            }
            return dataArray;

        } catch (Exception ioe) {
            log.debug("Excel Export - Reforming object array failed ", ioe);
            return data;
        }

    }

    /**
     * Expand Json objects to new columns, if there are object values in columns
     * @param data
     * @return
     */
    private JSONArray expandObjectValues(JSONArray data) {
        if (data == null) return data;
        JSONArray dataArray = new JSONArray();
        try {
            JSONArray hrow = data.getJSONArray(0);  // header row
            if (hrow == null) return data;

            for (int rowIndex = 1; rowIndex < data.length(); rowIndex++) {
                JSONArray row = data.getJSONArray(rowIndex);
                if (row == null) return data;

                JSONArray header = new JSONArray();
                JSONArray newdata = new JSONArray();
                for (int subIndex = 0; subIndex < row.length(); subIndex++) {
                    if (row.get(subIndex) instanceof JSONObject) {
                        Iterator<?> keys = row.getJSONObject(subIndex).keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            // append new columns to header row
                            if (rowIndex == 1) {
                                header.put(hrow.get(subIndex)+"_"+key);
                            }
                            newdata.put(row.getJSONObject(subIndex).get(key).toString());
                        }

                    } else {
                        // Add item
                        header.put(hrow.get(subIndex));
                        newdata.put(row.get(subIndex));
                    }
                }
                if (rowIndex == 1) {
                    dataArray.put(header);
                }
                dataArray.put(newdata);
            }
            return dataArray;

        } catch (Exception ioe) {
            log.debug("Excel Export - Expanding objects failed ", ioe);
            return data;
        }

    }

    /**
     * Check if there are json object column values
     * @param data
     * @return
     */
    private boolean hasObjectColumns(JSONArray data) {
        if (data == null) return false;

        try {
             // 0 row is for column titles
            for (int rowIndex = 1; rowIndex < data.length(); rowIndex++) {
                JSONArray row = data.getJSONArray(rowIndex);
                if (row == null) return false;

                for (int subIndex = 0; subIndex < row.length(); subIndex++) {
                    if (row.get(subIndex) instanceof JSONObject) {
                        return true;
                    }
                }

            }
            return false;

        } catch (Exception ioe) {
            log.debug("Excel Export - checking object columns failed ", ioe);
            return false;
        }

    }

    /**
     * Build metadata CSW url request
     * @param uuid
     * @return
     */
    private String getMetadataUrl(String uuid){
        String url = uuid;
        if(PropertyUtil.getOptional("service.metadata.url") != null) {
            url = PropertyUtil.getOptional("service.metadata.url") + "?Request=GetRecordById&service=CSW&version=2.0.2&id=";
            url = url + uuid;
        }
        return url;
    }
}
