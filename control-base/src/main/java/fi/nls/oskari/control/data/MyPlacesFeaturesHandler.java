package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.geo.Point;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.wfs.v1_0_0.WFSTransactionState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@OskariActionRoute("MyPlacesFeture")
public class MyPlacesFeaturesHandler extends ActionHandler {

    public void handleAction(ActionParameters params) throws ActionException {

        try {
            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
            b.setName("name");
            b.setCRS( DefaultGeographicCRS.WGS84 ); // set crs first
            b.add( "feature:geometry", Point.class ); // then add geometry
            b.add( "feature:name", String.class );
            b.add( "feature:place_desc", String.class );
            b.add( "feature:attention_text", String.class );
            final SimpleFeatureType featureType = b.buildFeatureType();

            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
            builder.add( new Point( 376415.4647, 6678470.1552 ) );
            builder.add( "Nimi" );
            builder.add( "Kuvaus" );
            builder.add( "Huomioteksti" );
            SimpleFeature feature = builder.buildFeature( "fid.1" );

            Transaction transaction = new DefaultTransaction("insert");
            Map connection = new HashMap();
            connection.put("url", "http://localhost:8080/geoserver/oskari/ows?");
            connection.put("userName", "admin");
            connection.put("password", "geoserver");

            // Step 2 - connection
            DataStore wfs = DataStoreFinder.getDataStore( connection );
            wfs.createSchema(featureType);
            SimpleFeatureStore featureStore = (SimpleFeatureStore) wfs.getFeatureSource(featureType.getTypeName());

            featureStore.setTransaction( transaction );
            featureStore.addFeatures( DataUtilities.collection( feature ) );
            transaction.commit();

            // get the final feature id
            WFSTransactionState state = (WFSTransactionState) transaction.getState(wfs);

            // In this example there is only one fid. Get it.
            String[] fids = state.getFids( "" );
            String result = fids[0];

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}