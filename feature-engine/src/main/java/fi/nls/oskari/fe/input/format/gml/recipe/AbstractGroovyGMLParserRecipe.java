package fi.nls.oskari.fe.input.format.gml.recipe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.xml.Configuration;
import org.xml.sax.SAXException;

import fi.nls.oskari.fe.input.format.gml.FEPullParser;
import fi.nls.oskari.fe.input.format.gml.FEPullParser.PullParserHandler;

public abstract class AbstractGroovyGMLParserRecipe extends
		AbstractGroovyXMLParserRecipe {

	public static abstract class GML3 extends AbstractGroovyGMLParserRecipe {
		{
			//gml = new org.geotools.gml3.GMLConfiguration();
		    gml = new GML31_Configuration();
			parserAny = new FEPullParser(gml, null);

		}

	}

	public static abstract class GML32 extends AbstractGroovyGMLParserRecipe {
		{
			gml = new org.geotools.gml3.v3_2.GMLConfiguration(true);
			parserAny = new FEPullParser(gml, null);
		}

	}
	
	public static abstract class GML2 extends AbstractGroovyGMLParserRecipe {
        {
            gml = new org.geotools.gml2.GMLConfiguration();
            parserAny = new FEPullParser(gml, null);
        }

    }


	protected Configuration gml;

	protected FEPullParser parserAny;

	public Map<QName, FEPullParser.PullParserHandler> mapGeometryType(
			final QName qname) {
		Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();
		handlers.put(qname, new FEPullParser.ElementPullParserHandler(qname,
				gml));
		return handlers;
	}

	public Map<QName, FEPullParser.PullParserHandler> mapGeometryTypes(
			final String ns, final String... localNames) {
		Map<QName, FEPullParser.PullParserHandler> handlers = new HashMap<QName, FEPullParser.PullParserHandler>();

		for (String localPart : localNames) {
			QName qname = new QName(ns, localPart);
			handlers.put(qname, new FEPullParser.ElementPullParserHandler(
					qname, gml));
		}

		return handlers;

	}

	public Object parseGeometry(
			Map<QName, FEPullParser.PullParserHandler> handlers,
			SMInputCursor crsr) throws XMLStreamException, IOException,
			SAXException {
		QName qn = crsr.getQName();
		PullParserHandler handler = handlers.get(qn);
		Object obj = null;
		if (handler != null) {
			parserAny.setHandler(handler);
			parserAny.setPp(crsr.getStreamReader());

			obj = parserAny.parse();
		}

		return obj;
	}

}
