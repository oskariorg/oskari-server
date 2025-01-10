package org.geotools.mif.util;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FieldSplitterTest {

    @Test
    public void bunchOfQuotes() {
        String str = "0,0,\"516\",\"Lappi\",\"Lappland\",\"http://www.kela.fi\",\"\",";
        String delimiter = ",";
        MIDFieldSplitter splitter = new MIDFieldSplitter(str, delimiter);
        Assertions.assertEquals("0", splitter.next());
        Assertions.assertEquals("0", splitter.next());
        Assertions.assertEquals("516", splitter.next());
        Assertions.assertEquals("Lappi", splitter.next());
        Assertions.assertEquals("Lappland", splitter.next());
        Assertions.assertEquals("http://www.kela.fi", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals(null, splitter.next());
    }

    @Test
    public void bunchOfEmptyValues() {
        String str = "foo,,bar,,,baz,qux";
        String delimiter = ",";
        MIDFieldSplitter splitter = new MIDFieldSplitter(str, delimiter);
        Assertions.assertEquals("foo", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals("bar", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals("baz", splitter.next());
        Assertions.assertEquals("qux", splitter.next());
        Assertions.assertEquals(null, splitter.next());
    }

    @Test
    public void testMultiCharDelimiter() {
        String str = "foo, , bar, , , baz, qux";
        String delimiter = ", ";
        MIDFieldSplitter splitter = new MIDFieldSplitter(str, delimiter);
        Assertions.assertEquals("foo", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals("bar", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals("", splitter.next());
        Assertions.assertEquals("baz", splitter.next());
        Assertions.assertEquals("qux", splitter.next());
        Assertions.assertEquals(null, splitter.next());
    }

}
