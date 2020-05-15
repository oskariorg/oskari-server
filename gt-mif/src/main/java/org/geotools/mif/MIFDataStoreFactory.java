package org.geotools.mif;

import java.awt.RenderingHints.Key;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.KVP;
import org.geotools.util.URLs;

public class MIFDataStoreFactory implements DataStoreFactorySpi {

    public static final Param MIF_FILE_PARAM = new Param(
            "url",
            URL.class,
            "MIF file",
            true,
            null,
            new KVP(Param.EXT, "mif"));

    private Boolean isAvailable = null;

    public MIFDataStoreFactory() {}

    @Override
    public String getDisplayName() {
        return "MIF";
    }

    @Override
    public String getDescription() {
        return "MapInfo(R) Data Interchange Format";
    }

    @Override
    public Param[] getParametersInfo() {
        return new Param[] { MIF_FILE_PARAM };
    }

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        try {
            URL url = (URL) MIF_FILE_PARAM.lookUp(params);
            if (url == null) {
                return false;
            }
            File mif = URLs.urlToFile(url);
            return mif.canRead();
        } catch (IOException ignore) {
            // Ignore
        }
        return false;
    }

    @Override
    public synchronized boolean isAvailable() {
        if (isAvailable == null) {
            try {
                Class.forName("org.geotools.mif.MIFDataReader");
                isAvailable = true;
            } catch (ClassNotFoundException e) {
                isAvailable = false;
            }
        }
        return isAvailable;
    }

    @Override
    public Map<Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        URL url = (URL) MIF_FILE_PARAM.lookUp(params);
        File mif = URLs.urlToFile(url);
        File mid = getAssistingMIDFile(mif);
        return new MIFDataStore(mif, mid);
    }

    private File getAssistingMIDFile(File mif) {
        String name = mif.getName();
        String nameNoF = name.substring(0, name.length() - 1);
        char f = name.charAt(name.length() - 1);
        char d = f;
        // f -> d, F -> D
        d += 'd' - 'f';
        String mid = nameNoF + d;
        File parent = mif.getParentFile();
        return new File(parent, mid);
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        throw new UnsupportedOperationException("MIF Datastore is read only");
    }

}
