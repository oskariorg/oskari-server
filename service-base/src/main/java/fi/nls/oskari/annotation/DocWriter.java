package fi.nls.oskari.annotation;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Writes an md document under docs/routes/[routename].md. Used when parsing custom annotation
 * to write the javadoc comment into a file
 */
public class DocWriter {

    private final ProcessingEnvironment environment;

    public DocWriter(final ProcessingEnvironment environment) {

        if(environment == null) {
            throw new IllegalArgumentException(
                    "ProcessingEnvironment not specified");
        }

        this.environment = environment;
    }

    private String getFileName(final String route) {
        return "docs/routes/" + route + ".md";
    }

    /**
     * Writes the route documentation to a file
     *
     * @param location the compiler location to write the services file to
     * @throws java.io.IOException if the services file cannot be written
     */
    public void write(final Location location, final String route, final String documentation) throws IOException {
        final FileObject file = environment.getFiler().createResource(
                location, "", getFileName(route));
        final Writer out = file.openWriter();
        final PrintWriter pw = new PrintWriter(out);
        pw.print(documentation);
        pw.flush();
        pw.close();
    }
}
