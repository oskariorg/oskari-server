package org.oskari.map.userlayer.input;

import java.io.File;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.service.ServiceException;

public interface FeatureCollectionParser {

    public SimpleFeatureCollection parse(File file, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws ServiceException;

}
