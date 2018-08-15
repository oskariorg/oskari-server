package org.oskari.print.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<E> implements Enumeration<E> {

    private final Iterator<E> it;

    public IteratorEnumeration(Iterator<E> it) {
        this.it = it;
    }

    @Override
    public boolean hasMoreElements() {
        return it.hasNext();
    }

    @Override
    public E nextElement() {
        return it.next();
    }

}
