package fi.nls.oskari.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by TMIKKOLAINEN on 30.12.2014.
 */
public class XLSXStreamer implements TabularFileStreamer {
    @Override
    public void writeToStream(String[] headers, Object[][] data, Map<String, Object> additionalFields, OutputStream out) throws IOException {
        Workbook wb = new SXSSFWorkbook();
        Sheet sh = wb.createSheet();
        Object[] rowArray;
        int cellNum, rowNum = 0;
        Row row = sh.createRow(rowNum);
        Cell cell;
        Object value;
        for (cellNum = 0; cellNum < headers.length; cellNum++) {
            cell = row.createCell(cellNum);
            fillCell(cell, headers[cellNum]);
        }
        for (rowNum = 1; rowNum < data.length+1; rowNum++) {
            rowArray = data[rowNum-1];
            row = sh.createRow(rowNum);
            for (cellNum = 0; cellNum < rowArray.length; cellNum++) {
                value = rowArray[cellNum];
                cell = row.createCell(cellNum);
                fillCell(cell, value);
            }
        }

        // TODO see if additional fields can be put in metadata...
        if (!additionalFields.isEmpty()) {
            row = sh.createRow(rowNum);
            rowNum++;
        }
        for (Map.Entry<String, Object> entry : additionalFields.entrySet()) {
            row = sh.createRow(rowNum);
            cell = row.createCell(0);
            fillCell(cell, entry.getKey());
            cell = row.createCell(1);
            fillCell(cell, entry.getValue());
            rowNum++;
        }

        wb.write(out);
        out.flush();
        out.close();
    }

    private void fillCell(Cell cell, Object value) {
        if (value == null) {
            cell.setCellType(CellType.BLANK);
        } else if (value instanceof String) {
            cell.setCellType(CellType.STRING);
            cell.setCellValue((String)value);
        } else if (value instanceof Double) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Double)value);
        } else if (value instanceof Float) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Float)value);
        } else if (value instanceof Integer) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Integer)value);
        } else if (value instanceof Long) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Long)value);
        } else if (value instanceof Short) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Short) value);
        } else if (value instanceof Boolean) {
            cell.setCellType(CellType.BOOLEAN);
            cell.setCellValue((Boolean)value);
        } else {
            // Object or an Array...
            cell.setCellType(CellType.STRING);
            cell.setCellValue(value.toString());
        }
    }
}
