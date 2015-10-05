package fi.nls.oskari.printout.ws.jaxrs.resource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.ws.WsTestResources;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

public class FontPostJsonTest {

	final int port = 8888;
	final String pngOutputFileNameTemplate = "test-output/ws-fontTest-%1$s-%2$s-%3$s-%4$s.png";
	final String pdfOutputFileNameTemplate = "test-output/ws-fontTest-%1$s-%2$s-%3$s-%4$s.pdf";

	

	@Test
	public void testPostFontLiberationMonoJSONForPNG() throws IOException,
			URISyntaxException {

		Client c = Client.create();
		URI uri = WsTestResources
				.getTestResource("/imaging/service/thumbnail/maplinkgeojson.png");
		WebResource r = c.resource(uri);

		StringWriter w = new StringWriter();
		InputStream req = MapProducer.class
				.getResourceAsStream("geojsPrintTest20130423-Liberation-Mono.json");

		try {
			IOUtils.copy(req, w);
		} finally {
			req.close();
		}

		ClientResponse response = r.accept("image/png")
				.type("application/json").entity(w.getBuffer().toString())
				.post(ClientResponse.class);

		InputStream in = response.getEntityInputStream();
		try {
			final String fn = String.format(pngOutputFileNameTemplate,
					"geojsPrintTest20130423", "Liberation-Mono", "default",
					"default");
			FileOutputStream outf = new FileOutputStream(fn);
			try {
				IOUtils.copy(in, outf);

			} finally {
				outf.close();
			}
		} finally {
			in.close();
		}

	}
	

	@Test
	public void testPostFontLiberationMonoJSONForPDF() throws IOException,
			URISyntaxException {

		Client c = Client.create();
		URI uri = WsTestResources
				.getTestResource("/imaging/service/thumbnail/maplinkgeojson.pdf");
		WebResource r = c.resource(uri);

		StringWriter w = new StringWriter();
		InputStream req = MapProducer.class
				.getResourceAsStream("geojsPrintTest20130423-Liberation-Mono.json");

		try {
			IOUtils.copy(req, w);
		} finally {
			req.close();
		}

		ClientResponse response = r.accept("application/pdf")
				.type("application/json").entity(w.getBuffer().toString())
				.post(ClientResponse.class);

		InputStream in = response.getEntityInputStream();
		try {
			final String fn = String.format(pdfOutputFileNameTemplate,
					"geojsPrintTest20130423", "Liberation-Mono", "default",
					"default");
			FileOutputStream outf = new FileOutputStream(fn);
			try {
				IOUtils.copy(in, outf);

			} finally {
				outf.close();
			}
		} finally {
			in.close();
		}

	}

	@Test
	public void testPostFontTestTemplateJSONForPNG() throws IOException,
			URISyntaxException {

		Client c = Client.create();
		URI uri = WsTestResources
				.getTestResource("/imaging/service/thumbnail/maplinkgeojson.png");
		WebResource r = c.resource(uri);

		StringWriter w = new StringWriter();

		InputStream req = MapProducer.class
				.getResourceAsStream("geojsPrintTest20130423-FontTestTemplate.json");

		try {
			IOUtils.copy(req, w);
		} finally {
			req.close();
		}
		
		String[] fonts = {

				"Bitstream Charter", "Century Schoolbook L", "Dialog", "DialogInput",
						"Dingbats", "Hershey", "Liberation Mono", "Liberation Sans",
						"Liberation Serif", "Monospaced", "Nimbus Mono L",
						"Nimbus Roman No9 L", "Nimbus Sans L", "SansSerif", "Serif",
						"Standard Symbols L", "URW Bookman L", "URW Chancery L",
						"URW Gothic L", "URW Palladio L", "Utopia" };

				String[] fontWeights = { "normal", "bold" };

				String[] fontSizes = { "9px", "12px", "22px" };

		for (final String font : fonts) {

			for (final String fontWeight : fontWeights) {

				for (final String fontSize : fontSizes) {

					String json = w.getBuffer().toString()
							.replaceAll("FONTFAMILYPLACEHOLDER", font);
					json = json.replaceAll("FONTSIZEPLACEHOLDER", fontSize);
					json = json.replaceAll("FONTWEIGHTPLACEHOLDER", fontWeight);
					assertTrue(json.indexOf(font) != -1);
					assertTrue(json.indexOf(fontSize) != -1);
					assertTrue(json.indexOf(fontWeight) != -1);

					ClientResponse response = r.accept("image/png")
							.type("application/json").entity(json)
							.post(ClientResponse.class);

					InputStream in = response.getEntityInputStream();
					try {
						final String fn = String.format(pngOutputFileNameTemplate,
								"geojsPrintTest20130423", font, fontSize,
								fontWeight);
						FileOutputStream outf = new FileOutputStream(fn);
						try {
							IOUtils.copy(in, outf);

						} finally {
							outf.close();
						}
					} finally {
						in.close();
					}
				}
			}
		}

	}
	
	@Test
	public void testPostStatsTestTemplateJSONForPNG() throws IOException,
			URISyntaxException {

		Client c = Client.create();
		URI uri = WsTestResources
				.getTestResource("/imaging/service/thumbnail/maplinkgeojson.png");
		WebResource r = c.resource(uri);

		StringWriter w = new StringWriter();

		InputStream req = MapProducer.class
				.getResourceAsStream("testStatLayerPrint-FontTestTemplate.json");

		try {
			IOUtils.copy(req, w);
		} finally {
			req.close();
		}
		
		String[] fonts = {

				"Bitstream Charter", "Century Schoolbook L", "Dialog", "DialogInput",
						"Dingbats", "Hershey", "Liberation Mono", "Liberation Sans",
						"Liberation Serif", "Monospaced", "Nimbus Mono L",
						"Nimbus Roman No9 L", "Nimbus Sans L", "SansSerif", "Serif",
						"Standard Symbols L", "URW Bookman L", "URW Chancery L",
						"URW Gothic L", "URW Palladio L", "Utopia" };

		String[] fontWeights = { "normal", "bold" };
		String[] fontSizes = { "9px", "10px", "11px" , "12px", "13px", "14px", "15px" };

		for (final String font : fonts) {

			for (final String fontWeight : fontWeights) {

				for (final String fontSize : fontSizes) {

					String json = w.getBuffer().toString()
							.replaceAll("FONTFAMILYPLACEHOLDER", font);
					json = json.replaceAll("FONTSIZEPLACEHOLDER", fontSize);
					json = json.replaceAll("FONTWEIGHTPLACEHOLDER", fontWeight);
					assertTrue(json.indexOf(font) != -1);
					assertTrue(json.indexOf(fontSize) != -1);
					assertTrue(json.indexOf(fontWeight) != -1);

					ClientResponse response = r.accept("image/png")
							.type("application/json").entity(json)
							.post(ClientResponse.class);

					InputStream in = response.getEntityInputStream();
					try {
						final String fn = String.format(pngOutputFileNameTemplate,
								"testStatLayerPrint", font, fontSize,
								fontWeight);
						FileOutputStream outf = new FileOutputStream(fn);
						try {
							IOUtils.copy(in, outf);

						} finally {
							outf.close();
						}
					} finally {
						in.close();
					}
				}
			}
		}

	}
	
	@Test
	public void testPostStatsTestTemplateJSONForPDF() throws IOException,
			URISyntaxException {

		Client c = Client.create();
		URI uri = WsTestResources
				.getTestResource("/imaging/service/thumbnail/maplinkgeojson.pdf");
		WebResource r = c.resource(uri);

		StringWriter w = new StringWriter();

		InputStream req = MapProducer.class
				.getResourceAsStream("testStatLayerPrint-FontTestTemplate.json");

		try {
			IOUtils.copy(req, w);
		} finally {
			req.close();
		}
		
		String[] fonts = {

				"Bitstream Charter", "Century Schoolbook L", "Dialog", "DialogInput",
						"Dingbats", "Hershey", "Liberation Mono", "Liberation Sans",
						"Liberation Serif", "Monospaced", "Nimbus Mono L",
						"Nimbus Roman No9 L", "Nimbus Sans L", "SansSerif", "Serif",
						"Standard Symbols L", "URW Bookman L", "URW Chancery L",
						"URW Gothic L", "URW Palladio L", "Utopia" };

		String[] fontWeights = { "normal", "bold" };
		String[] fontSizes = { "9px", "10px", "11px" , "12px", "13px", "14px", "15px" };
		
		int total = fonts.length * fontWeights.length * fontSizes.length;
		int pc = 0;
		int counter = 0;

		for (final String font : fonts) {

			for (final String fontWeight : fontWeights) {

				for (final String fontSize : fontSizes) {

					String json = w.getBuffer().toString()
							.replaceAll("FONTFAMILYPLACEHOLDER", font);
					json = json.replaceAll("FONTSIZEPLACEHOLDER", fontSize);
					json = json.replaceAll("FONTWEIGHTPLACEHOLDER", fontWeight);
					assertTrue(json.indexOf(font) != -1);
					assertTrue(json.indexOf(fontSize) != -1);
					assertTrue(json.indexOf(fontWeight) != -1);

					ClientResponse response = r.accept("application/pdf")
							.type("application/json").entity(json)
							.post(ClientResponse.class);

					InputStream in = response.getEntityInputStream();
					try {
						final String fn = String.format(pdfOutputFileNameTemplate,
								"testStatLayerPrint", font, fontSize,
								fontWeight);
						FileOutputStream outf = new FileOutputStream(fn);
						try {
							IOUtils.copy(in, outf);

						} finally {
							outf.close();
						}
					} finally {
						in.close();
					}
					
					counter++;
					
					int nextpc = 100 * counter / total ;
					
					if( nextpc != pc ) {
						pc = nextpc;
						System.out.println(""+pc+"%");
					}
					
				}
			}
		}

	}
}
