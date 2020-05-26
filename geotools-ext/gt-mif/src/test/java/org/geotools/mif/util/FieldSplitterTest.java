package org.geotools.mif.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FieldSplitterTest {

    @Test
    public void bunchOfQuotes() {
        String str = "0,0,\"516\",\"Lappi\",\"Lappland\",\"http://www.kela.fi\",\"\",";
        String delimiter = ",";
        MIDFieldSplitter splitter = new MIDFieldSplitter(str, delimiter);
        assertEquals("0", splitter.next());
        assertEquals("0", splitter.next());
        assertEquals("516", splitter.next());
        assertEquals("Lappi", splitter.next());
        assertEquals("Lappland", splitter.next());
        assertEquals("http://www.kela.fi", splitter.next());
        assertEquals("", splitter.next());
        assertEquals("", splitter.next());
        assertEquals(null, splitter.next());
    }

    @Test
    public void bunchOfEmptyValues() {
        String str = "foo,,bar,,,baz,qux";
        String delimiter = ",";
        MIDFieldSplitter splitter = new MIDFieldSplitter(str, delimiter);
        assertEquals("foo", splitter.next());
        assertEquals("", splitter.next());
        assertEquals("bar", splitter.next());
        assertEquals("", splitter.next());
        assertEquals("", splitter.next());
        assertEquals("baz", splitter.next());
        assertEquals("qux", splitter.next());
        assertEquals(null, splitter.next());
    }

    @Test
    public void testMultiCharDelimiter() {
        String str = "foo, , bar, , , baz, qux";
        String delimiter = ", ";
        MIDFieldSplitter splitter = new MIDFieldSplitter(str, delimiter);
        assertEquals("foo", splitter.next());
        assertEquals("", splitter.next());
        assertEquals("bar", splitter.next());
        assertEquals("", splitter.next());
        assertEquals("", splitter.next());
        assertEquals("baz", splitter.next());
        assertEquals("qux", splitter.next());
        assertEquals(null, splitter.next());
    }

}
