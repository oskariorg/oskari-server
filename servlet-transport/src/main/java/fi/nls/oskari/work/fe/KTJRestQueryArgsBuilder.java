package fi.nls.oskari.work.fe;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import org.apache.http.client.utils.URIBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.util.List;

/* A PoC LEGACY Request args builder */
public class KTJRestQueryArgsBuilder implements FEQueryArgsBuilder {

    @Override
    public void buildParams(URIBuilder builder, JobType type, WFSLayerStore layer,
            SessionStore session, List<Double> bounds, MathTransform transform,
            CoordinateReferenceSystem crs) {
        // TODO Auto-generated method stub

        /*
         * ?"+
         * "coords=%445205.484+7196497.907&projection=NLSFI%3Aeuref&buffer=100&desimaali.lkm.max=3&sijaintiformaatti=&
         */

        builder.setParameter("projection", "NLSFI:euref");
        builder.setParameter("buffer", Integer.toString(0, 10));
        builder.setParameter("desimaali.lkm.max", Integer.toString(3, 10));
        builder.setParameter("sijaintiformaatti", "");

        StringBuffer coordsAsSpaceSeparatedString = new StringBuffer();
        if (type == JobType.MAP_CLICK) {

            Coordinate c = session.getMapClick();

            // seems this is not needed here since it isn't used,
            // but could be used for checking for valid envelope so leaving it in code
            new ReferencedEnvelope(new Envelope(c), crs);
            /* env.expandBy(10); */

            coordsAsSpaceSeparatedString.append(Double.toString(c
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(c
                    .getOrdinate(1)));

            builder.setParameter("buffer", Integer.toString(100, 10));

        } else if (bounds != null) {

            ReferencedEnvelope env = new ReferencedEnvelope(
                    new Envelope(bounds.get(0), bounds.get(2), bounds.get(1),
                            bounds.get(3)), crs);
            // env.expandBy(300);

            DirectPosition upperCorner = env.getUpperCorner();
            DirectPosition lowerCorner = env.getLowerCorner();

            /* left, top */
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* right, top */
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* right, bottom */
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* left, bottom */
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* left, top */
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(1)));

        } else {
            ReferencedEnvelope env = session.getLocation().getEnvelope();
            // env.expandBy(300);

            DirectPosition upperCorner = env.getUpperCorner();
            DirectPosition lowerCorner = env.getLowerCorner();

            /* left, top */
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* right, top */
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* right, bottom */
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* left, bottom */
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(1)));
            coordsAsSpaceSeparatedString.append(' ');
            /* left, top */
            coordsAsSpaceSeparatedString.append(Double.toString(lowerCorner
                    .getOrdinate(0)));
            coordsAsSpaceSeparatedString.append(' ');
            coordsAsSpaceSeparatedString.append(Double.toString(upperCorner
                    .getOrdinate(1)));
        }

        builder.setParameter("coords", coordsAsSpaceSeparatedString.toString());

    }

}