package org.oskari.service.wfs3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.api.feature.FeatureVisitor;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.sort.SortBy;
import org.geotools.api.util.ProgressListener;

public class PaginatedFeatureCollection implements SimpleFeatureCollection {

    private final List<SimpleFeatureCollection> pages;
    private final SimpleFeatureType schema;
    private final String collectionId;
    private final int maxSize;

    public PaginatedFeatureCollection(List<SimpleFeatureCollection> pages,
            SimpleFeatureType schema, String collectionId, int maxSize) {
        this.pages = pages;
        this.schema = schema;
        this.collectionId = collectionId != null ? collectionId : "featureCollection";
        this.maxSize = maxSize;
    }

    @Override
    public SimpleFeatureType getSchema() {
        return schema;
    }

    @Override
    public String getID() {
        return collectionId;
    }

    @Override
    public void accepts(FeatureVisitor visitor, ProgressListener progress) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReferencedEnvelope getBounds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        for (SimpleFeatureCollection page : pages) {
            if (page.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> o) {
        for (Object a : o) {
            if (!contains(a)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        for (SimpleFeatureCollection page : pages) {
            if (!page.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        int sum = pages.stream()
                .mapToInt(SimpleFeatureCollection::size)
                .sum();
        return Math.min(sum, maxSize);
    }

    @Override
    public Object[] toArray() {
        Object[] a = new Object[size()];
        int i = 0;
        for (SimpleFeatureCollection page : pages) {
            try (SimpleFeatureIterator it = page.features()) {
                while (it.hasNext() && i < a.length) {
                    a[i++] = it.next();
                }
            }
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <O> O[] toArray(O[] a) {
        int n = size();
        if (a.length < n) {
            return (O[]) toArray();
        }
        int i = 0;
        for (SimpleFeatureCollection page : pages) {
            try (SimpleFeatureIterator it = page.features()) {
                while (it.hasNext() && i < n) {
                    a[i++] = (O) it.next();
                }
            }
        }
        return a;
    }

    @Override
    public SimpleFeatureIterator features() {
        return new PaginatedIterator(pages.iterator(), maxSize);
    }

    @Override
    public SimpleFeatureCollection subCollection(Filter filter) {
        List<SimpleFeatureCollection> filteredPages = new ArrayList<>();  
        for (SimpleFeatureCollection page : pages) {
            SimpleFeatureCollection f = page.subCollection(filter);
            if (!f.isEmpty()) {
                filteredPages.add(f);
            }
        }
        return new PaginatedFeatureCollection(filteredPages, schema, "subCollection", maxSize);
    }

    @Override
    public SimpleFeatureCollection sort(SortBy order) {
        throw new UnsupportedOperationException();
    }

    class PaginatedIterator implements SimpleFeatureIterator {

        private Iterator<SimpleFeatureCollection> pageIterator;
        private SimpleFeatureIterator featureIterator;
        private SimpleFeature next;
        private boolean closed;
        private int i;
        private int maxSize;

        private PaginatedIterator(Iterator<SimpleFeatureCollection> pageIterator, int maxSize) {
            this.pageIterator = pageIterator;
            this.featureIterator = pageIterator.hasNext() ? pageIterator.next().features() : null;
            this.closed = featureIterator == null;
            this.i = 0;
            this.maxSize = maxSize;
        }

        @Override
        public boolean hasNext() {
            if (closed) {
                return false;
            }
            if (next != null) {
                return true;
            }
            while (!featureIterator.hasNext()) {
                if (!pageIterator.hasNext()) {
                    close();
                    return false;
                }
                featureIterator = pageIterator.next().features();
            }
            next = featureIterator.next();
            return true;
        }

        @Override
        public SimpleFeature next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            SimpleFeature tmp = next;
            next = null;
            if (++i >= maxSize) {
                close();
            }
            return tmp;
        }

        @Override
        public void close() {
            if (featureIterator != null) {
                featureIterator.close();
            }
            this.closed = true;
        }

    }

}
