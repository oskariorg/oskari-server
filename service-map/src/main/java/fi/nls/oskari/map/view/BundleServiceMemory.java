package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Not thread-safe implementation of BundleService
 * Stores the objects in an ArrayList
 */
public class BundleServiceMemory implements BundleService {

    private final List<Bundle> list = new ArrayList<>();

    private int indexOf(int id) {
        for (int i = 0; i < list.size(); i++) {
            Bundle item = list.get(i);
            if (item.getBundleId() == id) {
                return i;
            }
        }
        return -1;
    }

    private int indexOf(String name) {
        if (name != null && !name.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                Bundle item = list.get(i);
                if (name.equals(item.getName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public Bundle find(int id) {
        for (Bundle item : list) {
            if (item.getBundleId() == id) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Bundle find(String id) {
        if (id != null && !id.isEmpty()) {
            for (Bundle item : list) {
                if (id.equals(item.getName())) {
                    return item;
                }
            }
        }
        return null;
    }

    @Override
    public List<Bundle> findAll() {
        return new ArrayList<>(list);
    }

    @Override
    public void update(Bundle layerClass) {
        int i = indexOf(layerClass.getName());
        if (i >= 0) {
            list.set(i, layerClass);
        }
    }

    @Override
    public int insert(Bundle layerClass) {
        int seq = list.size();
        list.add(layerClass);
        layerClass.setBundleId(seq);
        return seq;
    }

    @Override
    public void delete(int id) {
        int i = indexOf(id);
        if (i >= 0) {
            list.remove(i);
        }
    }

    @Override
    public void delete(Map<String, String> parameterMap) {
        // Do nothing
    }

    @Override
    public Bundle getBundleTemplateByName(String name) {
        return find(name);
    }

    @Override
    public long addBundleTemplate(Bundle bundle) {
        return insert(bundle);
    }

    @Override
    public void forceBundleTemplateCached(String bundleid) {
        // Everything is cached
    }

}
