package fi.nls.oskari.annotation;

import fi.nls.oskari.control.ActionHandler;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Runs on compile time and processes any @OskariActionRoute annotation.
 * Checks that the annotated Class is a concrete Class that is assignable as fi.nls.oskari.control.ActionHandler.
 * If it isn't the compilation will fail. If it is an entry is written to an SPI services file to be used on runtime.
 * The file is created if it didn't exist and duplicates aren't written.
 */
@SupportedAnnotationTypes(OskariActionRouteAnnotationProcessor.ANNOTATION_TYPE)
public class OskariActionRouteAnnotationProcessor extends OskariBaseAnnotationProcessor {
    /**
     * This is the annotation we are going to process
     */
    public static final String ANNOTATION_TYPE = "fi.nls.oskari.annotation.OskariActionRoute";
    public static final String SERVICE_NAME = "fi.nls.oskari.control.ActionHandler";

    @Override
    public boolean process(
            final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        // we need the actual TypeElement of the annotation, not just it's name
        final TypeElement annotation = typeElement(ANNOTATION_TYPE);

        // make sure we have the annotation type
        if (annotation == null) {
            // no go for processing
            return false;
        }

        // get all classes that have the annotation
        final Set<? extends Element> annotatedElements =
                roundEnv.getElementsAnnotatedWith(annotation);

        try {
            // we will need to gather the annotated classes that we are
            // going to write to the services registration file
            final Set<String> results = new HashSet<String>(annotatedElements.size());

            for (final Element m : annotatedElements) {

                // we know @OskariActionRoute is a type annotation so casting to TypeElement
                final TypeElement el = (TypeElement) m;

                // check that the class is not abstract since we cant instantiate it if it is
                // and that its of the correct type for our fi.nls.oskari.control.ActionControl
                if (el.getModifiers().contains(Modifier.ABSTRACT) ||
                        !isAssignable(m.asType(), ActionHandler.class)) {
                    // wasn't proper annotation -> go for compilation failure
                    processingEnv.getMessager().printMessage(Kind.ERROR,
                            "@OskariActionRoute annotated classes must be non-abstract and of type " + ActionHandler.class, m);

                } else {
                    // we are good to go, gather it up in the results
                    OskariActionRoute route = el.getAnnotation(OskariActionRoute.class);
                    String comment = processingEnv.getElementUtils().getDocComment(el);
                    if (comment != null) {
                        writeDoc(route.value(), comment);
                    } else {
                        processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING,
                                "Documentation missing for route " + route.value() + " (" + el.getQualifiedName().toString() + ")");
                    }

                    results.add(el.getQualifiedName().toString());
                }
            }

            // write the services to file
            registerControls(results, SERVICE_NAME);
        } catch (final IOException ioe) {
            System.out.println("ERROR " + ioe.getMessage());
            processingEnv.getMessager().printMessage(
                    Kind.ERROR, "I/O Error during processing.");

            ioe.printStackTrace();
        }

        return true;
    }
}
