package fi.nls.oskari.printout.ws.jaxrs.resource;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import fi.nls.oskari.printout.output.map.MapProducer;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

public class PostJsonTestRunner {

	void run(String testname, URI input, PostJsonTestFileType output)
			throws IOException {
		Client c = Client.create();

		WebResource r = c.resource(input);

		StringWriter w = new StringWriter();

		InputStream req = MapProducer.class
				.getResourceAsStream(PostJsonTestFileType.JSON
						.getFilename(testname));

		try {
			IOUtils.copy(req, w);
		} finally {
			req.close();
		}

		ClientResponse response = r.accept(output.getMimeType())
				.type(PostJsonTestFileType.JSON.getMimeType()+";charset=UTF-8")
				.entity(w.getBuffer().toString()).post(ClientResponse.class);

		InputStream in = response.getEntityInputStream();
		try {
			FileOutputStream outf = new FileOutputStream(
					output.getFilename(testname));
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
