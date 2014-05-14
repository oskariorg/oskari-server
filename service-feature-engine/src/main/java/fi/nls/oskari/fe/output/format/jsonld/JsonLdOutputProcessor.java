package fi.nls.oskari.fe.output.format.jsonld;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.TurtleTripleCallback;
import com.github.jsonldjava.utils.JSONUtils;
import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.AbstractOutputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;

/* PoC that builds JSON-LD output */
public class JsonLdOutputProcessor extends AbstractOutputProcessor implements
		OutputProcessor {

	protected final String OSKARI_SPATIAL = "http://oskari.org/spatial#";

	protected final Resource VERTEX = Resource.iri(OSKARI_SPATIAL, "vertex");
	protected final Resource EDGE = Resource.iri(OSKARI_SPATIAL, "edge");

	protected final LinkedHashMap<String, Object> CONTEXT = new LinkedHashMap<String, Object>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5472266691674668312L;

		{
			put("oskari", OSKARI_SPATIAL);
			put("ogcf", "http://www.opengis.net/def/geosparql/function/");
			put("dc", "http://purl.org/dc/elements/1.1/");
			put("lgdo", "http://linkedgeodata.org/ontology/");
			put("lgdn", "http://linkedgeodata.org/resource/node/");
			put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
			put("lgdm", "http://linkedgeodata.org/meta/");
			put("foaf", "http://xmlns.com/foaf/0.1/");
			put("spatial", "http://geovocab.org/spatial#");
			put("gadm-r", "http://linkedgeodata.org/ld/gadm2/resource/");
			put("gadm-o", "http://linkedgeodata.org/ld/gadm2/ontology/");
			put("geom", "http://geovocab.org/geometry#");
			put("lgdw", "http://linkedgeodata.org/resource/way/");
			put("lgdwn", "http://linkedgeodata.org/resource/waynode/");
			put("meta", "http://linkedgeodata.org/ld/meta/ontology/");
			put("dcterms", "http://purl.org/dc/terms/");
			put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			put("lgd", "http://linkedgeodata.org/triplify/");
			put("ogc", "http://www.opengis.net/ont/geosparql#");
			put("lgd-geom", "http://linkedgeodata.org/geometry/");
			put("dbpedia", "http://localhost:8080/resource/");
			put("owl", "http://www.w3.org/2002/07/owl#");
			put("xsd", "http://www.w3.org/2001/XMLSchema#");
			put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		}
	};

	protected final ArrayList<Object> GRAPH = new ArrayList<Object>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8567372775982792897L;

		{

		}
	};

	protected final LinkedHashMap<String, Object> RESULT = new LinkedHashMap<String, Object>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8875492838913796265L;

		{
			// context
			put("@context", CONTEXT);
			put("@graph", GRAPH);
		}
	};

	public JsonLdOutputProcessor() {
		for (String key : CONTEXT.keySet()) {
			nsToPrefix.put((String) CONTEXT.get(key), key);
		}
	}

	public void begin() {
	}

	protected void debugPrint() throws JsonGenerationException, IOException {
		final OutputStreamWriter w = new OutputStreamWriter(System.out, "UTF-8");

		{
			JSONUtils.writePrettyPrint(w, RESULT);

			w.write("-------------\n");
			w.write("Generating Turtle Report\n");
			final JsonLdOptions options = new JsonLdOptions("") {
				{
					format = "text/turtle";
					useNamespaces = true;
				}
			};
			String rdf;
			try {
				rdf = (String) JsonLdProcessor.toRDF(RESULT,
						new TurtleTripleCallback(), options);
			} catch (JsonLdError e) {
				throw new IOException(e);
			}

			w.write(rdf);

			w.write('\n');
			;
			w.write("Generated Turtle Report\n");
		}

		w.flush();

		w.close();
	}

	public void edge(final Resource inV, final Resource predicate,
			final Resource outV) {
		/* optionally create an edge */
		final LinkedHashMap<String, Object> feat = new LinkedHashMap<String, Object>();
		feat.put("@id", prefixedResource(inV));
		feat.put("@type", prefixedResource(EDGE));

		LinkedHashMap<String, Object> linkVal = new LinkedHashMap<String, Object>();
		linkVal.put("@id", prefixedResource(outV));

		feat.put(prefixedResource(predicate), linkVal);

		GRAPH.add(feat);
	}

	public void end() throws IOException {
		debugPrint();

	}

	public void flush() {
	}

	public void prefix(String prefix, String ns) throws IOException {

		if (CONTEXT.get(prefix) != null) {
			if (!ns.equals((String) CONTEXT.get(prefix))) {
				throw new IOException("NS prefix conflict " + prefix + " [ "
						+ ns + " vs mapped " + CONTEXT.get(prefix) + "]");
			}
		} else {
			CONTEXT.put(prefix, ns);
			nsToPrefix.put(ns, prefix);
		}

	}

	public void type(Resource type,
			List<Pair<Resource, XSDDatatype>> simpleProperties,
			List<Pair<Resource, Object>> linkProperties,
			List<Pair<Resource, String>> geometryProperties) throws IOException {

	}

	public void vertex(Resource iri, Resource type,
			List<Pair<Resource, ?>> simpleProperties,
			List<Pair<Resource, ?>> linkProperties) throws IOException {

		final LinkedHashMap<String, Object> feat = new LinkedHashMap<String, Object>();
		feat.put("@id", prefixedResource(iri));
		feat.put("@type", prefixedResource(type));

		for (final Pair<Resource, ?> prop : simpleProperties) {
			if (prop.getValue() == null) {
				continue;
			}
			feat.put(prefixedResource(prop.getKey()), prop.getValue());
		}

		GRAPH.add(feat);

	}

	public void vertex(final Resource iri, final Resource type,
			final List<Pair<Resource, ?>> simpleProperties,
			final List<Pair<Resource, ?>> linkProperties,
			final List<Pair<Resource, Geometry>> geometryProperties)
			throws IOException {

		final LinkedHashMap<String, Object> feat = new LinkedHashMap<String, Object>();
		feat.put("@id", prefixedResource(iri));
		feat.put("@type", prefixedResource(type));

		for (final Pair<Resource, ?> prop : simpleProperties) {
			if (prop.getValue() == null) {
				continue;
			}
			feat.put(prefixedResource(prop.getKey()), prop.getValue());
		}

		for (final Pair<Resource, Geometry> geomProp : geometryProperties) {
			/*
			 * "POLYGON(( -77.089005 38.913574, -77.029953 38.913574, -77.029953
			 * 38.886321, -77.089005 38.886321, -77.089005 38.913574
			 * ))"^^geo:wktLiteral)
			 */
			final Geometry geom = geomProp.getValue();
			if (geom == null) {
				continue;
			}

			LinkedHashMap<String, Object> geomVal = new LinkedHashMap<String, Object>();
			geomVal.put("@type", "ogc:wktLiteral");
			geomVal.put("@value", geom.toText());

			feat.put(prefixedResource(geomProp.getKey()), geomVal);

		}

		GRAPH.add(feat);

	}

}
