package org.oskari.wcs.util.small;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Simple immutable Set
 */
public class SmallSet<T> implements Set<T> {

    private final T[] a;
    private final int len;

    public SmallSet(T[] a) {
        this.a = a;
        this.len = a.length;
        assertUnique();
    }

    private void assertUnique() {
        for (int i = 0; i < len - 1; i++) {
            for (int j = i + 1; j < len; j++) {
                if (a[i].equals(a[j])) {
                    throw new IllegalArgumentException("Not unique");
                }
            }
        }
    }

    @Override
    public boolean contains(Object o) {
        for (T t : a) {
            if (t.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c != null) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return len == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int i;

            @Override
            public boolean hasNext() {
                return i < len;
            }

            @Override
            public T next() {
                return a[i++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public int size() {
        return len;
    }

    /* Rest of the methods are Unsupported */

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

}
