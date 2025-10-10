package fi.nls.test.util;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
public class XmlTestHelper {
    public static Diff compareXML(String request, String expected) {
        return DiffBuilder.compare(request)
            .withTest(expected)
            .ignoreComments()
            .ignoreWhitespace()
            .checkForSimilar() // .withDifferenceEvaluator(DifferenceEvaluators.ignoreDifferencesWithinCDATA()) -> are always considered similar in 2.x
            .build();
    }
}
