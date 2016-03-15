package fi.nls.oskari.annotation;

// This file is part of the evenlistener sample package from
// http://www.developer.com/java/other/article.php/3853556/Implement-Automatic-Discovery-in-Your-Java-Code-with-Annotations.htm
// just moved it to a more convenient package with the rest of the code

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * This class handles the registration of service implementations during
 * annotation processing. Because the annotation processor will only
 * pass in annotated elements that have not yet been compiled, we can't
 * be sure that there are not other elements that were processed in
 * a previous compilation run.
 * </p><p>
 * The {@code ServiceRegistration} class can be used to merge together
 * several service registration files, and add in new services that
 * have been created during the current execution. It will ensure that
 * each class has only been included in the file once, and will attempt
 * to maintain comments as they were included.
 * </p><p>
 * <b>Tool Note:</b> Some annotation processing implementations have
 * restrictions around reading and writing to certain paths, some also
 * do not support the required standards. In these cases this class will
 * emit a warning message.
 * </p>
 *
 * @author Jason Morris
 */
public class ServiceRegistration {

    private final ProcessingEnvironment environment;

    private final String className;

    /**
     * The classes that are already known to be in this file. If
     * this {@code Set} already contains a class-name, the name
     * won't be added to the {@link #lines} {@code List}.
     */
    private final Set<String> classes = new HashSet<String>();

    /**
     * This is the raw contents of the services file, that we
     * will eventually output to the class output directory.
     */
    private final List<String> lines = new ArrayList<String>();

    /**
     * Create a new, empty {@code ServiceRegistration} object, with a
     * given {@code ProcessingEnvironment} to work within and the
     * classname of the service it will register.
     *
     * @param environment the {@code ProcessingEnvironment} this
     *      {@code ServiceRegistration} will register services wiuthin
     * @param className the name of the service class to register
     */
    public ServiceRegistration(
            final ProcessingEnvironment environment,
            final String className) {

        if(environment == null) {
            throw new IllegalArgumentException(
                    "ProcessingEnvironment not specified");
        }

        if(className == null || className.isEmpty()) {
            throw new IllegalArgumentException(
                    "No service class name specified");
        }

        this.environment = environment;
        this.className = className;
    }

    private String getFileName() {
        return "META-INF/services/" + className;
    }

    /**
     * This is the read implementation used by the
     * {@link #read(JavaFileManager.Location)} to consume the
     * contents of the {@code Reader}, while ensuring we don't
     * register a particular implementation more than once.
     *
     * @param reader the {@code Reader} to consume
     * @throws IOException if the services file cannot be read
     */
    private void read(final Reader reader) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(reader);

        String currentLine = null;

        while((currentLine = bufferedReader.readLine()) != null) {
            // follow the pattern used by ServiceLoader (more or less)
            if(!currentLine.isEmpty() &&
                    currentLine.indexOf('#') == -1) {

                if(classes.add(currentLine)) {
                    lines.add(currentLine);
                }
            } else {
                lines.add(currentLine);
            }
        }
    }

    /**
     * Look for a services file registering the specified classname
     * implementations in the given {@code Location}. If one is found,
     * read it and merge it with the current data in this
     * {@code ServiceRegistration}.
     *
     * @param location the {@code Location} to look for the services file
     */
    public void read(final Location location) {
        final String fileName = getFileName();

        try {
            final Filer filer = environment.getFiler();
            final FileObject file = filer.getResource(
                    location, "", fileName);

            if(file.getLastModified() != 0) {
                final Reader reader = new InputStreamReader(
                        file.openInputStream());
                read(reader);
                reader.close();
            }
        } catch(final IOException ioe) {
            environment.getMessager().printMessage(
                    Kind.WARNING,
                    "I/O Error: I couldn't read " + fileName +
                            " from location " + location.getName());
        } catch(final Exception error) {
            environment.getMessager().printMessage(
                    Kind.WARNING,
                    "I couldn't read " + fileName +
                            " from location " + location.getName() +
                            ". Error details: " + error.toString());
        }
    }

    /**
     * Ensure that the given classname is in this {@code ServiceRegistration}.
     * This method will only have an effect if the given classname
     * doesn't already appear in this {@code ServiceRegistration}.
     *
     * @param className the fully qualified name of the class to add
     */
    public void addClass(final String className) {
        if(classes.add(className)) {
            lines.add(className);
        }
    }

    /**
     * Write the contents of this {@code ServiceRegistration} out to
     * the given {@code Writer}.
     *
     * @param out the {@code Writer} to write the class list out to
     * @see #write(Location)
     */
    public void write(final Writer out) {
        final PrintWriter pw = new PrintWriter(out);
        final int length = lines.size();

        for(int i = 0; i < length; i++) {
            pw.println(lines.get(i));
        }

        pw.flush();
        pw.close();
    }

    /**
     * Write the contents of this {@code ServiceRegistration} out to
     * the given {@code Location}. This is a simplified method of
     * writing the services file to the correct file in the given
     * {@code Location}.
     *
     * @param location the compiler location to write the services file to
     * @throws IOException if the services file cannot be written
     */
    public void write(final Location location) throws IOException {
        final FileObject file = environment.getFiler().createResource(
                location,
                "",
                getFileName());
        write(file.openWriter());
    }
}
