package fi.nls.oskari.printout.output.factory;

import org.geotools.renderer.style.MarkFactory;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;

import java.awt.*;

public class RenderSupportMarkFactory implements MarkFactory {

	
	public Shape getShape(Graphics2D graphics, Expression symbolUrl,
			Feature feature) throws Exception {
		return null;
	}

}
