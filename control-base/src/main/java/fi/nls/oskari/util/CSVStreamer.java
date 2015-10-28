package fi.nls.oskari.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by TMIKKOLAINEN on 30.12.2014.
 */
public class CSVStreamer implements TabularFileStreamer {
    private char delimiter = ',';

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public void writeToStream(String[] headers, Object[][] data, Map<String, Object> additionalFields, OutputStream out) throws IOException {
        int i;
        // Write BOM, Excel won't use UTF-8 without it...
        out.write(239);
        out.write(187);
        out.write(191);
        OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        // We can't use .withHeader(headers) as that requires the headers to be unique
        final CSVPrinter printer = new CSVPrinter(
                writer,
                CSVFormat.DEFAULT.withDelimiter(getDelimiter())
        );
        printer.printRecord(headers);
        for (i = 0; i < data.length; i++) {
            printer.printRecord(data[i]);
        }

        if (!additionalFields.isEmpty()) {
            printer.printRecord(new String[0]);
        }
        for (Map.Entry<String, Object> entry : additionalFields.entrySet())
        {
            printer.printRecord(entry.getKey(), entry.getValue());
        }
        writer.flush();
        writer.close();
        printer.close();
    }
}
