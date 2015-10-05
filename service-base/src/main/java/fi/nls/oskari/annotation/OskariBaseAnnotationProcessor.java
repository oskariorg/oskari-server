package fi.nls.oskari.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Collection;

/**
 * Base class for processing annotations, provides some nice-to-have utility functions.
 */
public abstract class OskariBaseAnnotationProcessor extends AbstractProcessor {
    private static DocWriter docWriter = null;

    /**
     * Writes a documentation file for the route.
     */
    public void writeDoc(final String route, final String documentation)
            throws IOException {
        if (docWriter == null) {
            docWriter = new DocWriter(processingEnv);
        }
        docWriter.write(StandardLocation.CLASS_OUTPUT, route, documentation);
    }

    /**
     * Writing the services to an SPI file so we can find the annotated classes
     * on runtime.
     *
     * @param implementations fully qualified class names of the annotated classes
     * @param serviceName fully qualified class name of the interface we
     *                    are going to look for with ServiceLoader.
     * @throws java.io.IOException
     */
    public void registerControls(
            final Collection<String> implementations, final String serviceName)
            throws IOException {

        final ServiceRegistration registration = new ServiceRegistration(
                processingEnv, serviceName);

        registration.read(StandardLocation.SOURCE_PATH);
        registration.read(StandardLocation.CLASS_PATH);

        for (final String h : implementations) {
            registration.addClass(h);
        }
        registration.write(StandardLocation.CLASS_OUTPUT);
    }

    /* ************************************
     * Convenience methods
     * ************************************
     */

    /**
     * Returns the TypeElement for given class
     * @param type
     * @return
     */
    public TypeElement typeElement(Class<?> type) {
        return typeElement(type.getName());
    }

    /**
     * Returns the TypeElement for given class name
     * @param className
     * @return
     */
    public TypeElement typeElement(String className) {
        return processingEnv.getElementUtils().getTypeElement(className);
    }

    /**
     * Checks if the TypeElement is an implementation of the baseType
     * @return
     */
    public boolean isAssignable(TypeElement subType, Class<?> baseType) {
        return isAssignable(subType.asType(), baseType);
    }

    /**
     * Checks if the TypeMirror is an implementation of the baseType
     * @return
     */
    public boolean isAssignable(TypeMirror subType, Class<?> baseType) {
        return isAssignable(subType, typeElement(baseType));
    }

    /**
     * Checks if the TypeMirror is an implementation of the baseType
     * @return
     */
    public boolean isAssignable(TypeMirror subType, TypeElement baseType) {
        return isAssignable(subType, baseType.asType());
    }

    /**
     * Checks if the TypeMirror is an implementation of the baseType
     * @return
     */
    public boolean isAssignable(TypeMirror subType, TypeMirror baseType) {
        final Types typeUtils = processingEnv.getTypeUtils();
        return typeUtils.isAssignable(typeUtils.erasure(subType), typeUtils.erasure(baseType));
    }

}
