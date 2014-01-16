package fi.nls.oskari.printout.ws.jaxrs.format;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/*
 * writes out image as a result from JAX-RS request
 */
public class StreamingOutputImpl implements StreamingOutput {

	enum ImageIOFormat {
		png, jpeg
	}

	BufferedImage image;;

	ImageIOFormat imageIoFormat;

	StreamingOutputImpl(BufferedImage image, ImageIOFormat format) {
		this.image = image;
		this.imageIoFormat = format;
	}

	
	public void write(OutputStream outs) throws IOException,
			WebApplicationException {

		try {
			ImageIO.write(image, imageIoFormat.toString(), outs);
		} finally {
			image.flush();
		}
	}

}
