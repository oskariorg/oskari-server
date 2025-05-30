package org.oskari.map.myfeatures.input;

import java.io.File;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceException;

public interface FeatureCollectionParser {

    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws ServiceException;
    public String getSuffix();

}
