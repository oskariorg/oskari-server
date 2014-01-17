package fi.nls.oskari.printout.output.layer;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.reactor.IOReactorException;
import org.geotools.data.Base64;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.opengis.feature.simple.SimpleFeature;

import fi.nls.oskari.printout.caching.jedis.JedisCache;
import fi.nls.oskari.printout.input.layers.LayerDefinition;

/**
 * 
 * This class Loads layer tiles using async http client. Draws tiles to a layer
 * specific canvas and finally anchors canvas to resulting image.
 * 
 */
public class AsyncDirectTileLayer extends DirectTileLayer {
	enum SupportedDataUrlFormat {
		PNG_Base64("data:image/png;base64"), JPEG_Base64(
				"data:image/jpeg;base64");

		private String prefix;

		SupportedDataUrlFormat(String prefix) {
			this.prefix = prefix;
		}

		boolean match(String dataUrl) {
			return dataUrl.startsWith(prefix);
		}

		byte[] parse(String dataUrl) {

			String[] parts = dataUrl.split(",");
			if (!match(parts[0])) {
				return null;
			}
			String base64content = parts[1];

			return Base64.decode(base64content);

		}

	}

	AsyncLayerProcessor asyncProc;
	protected long timeoutInSeconds = 16;

	protected CountDownLatch latch;

	protected String xClientInfo;
	private boolean useCache;

	public AsyncDirectTileLayer(LayerDefinition ld,
			AsyncLayerProcessor asyncProc, DefaultFeatureCollection fc,
			AffineTransform transform, String xClientInfo,
			long timeoutInSeconds, boolean useCache) throws IOReactorException {
		super(ld, fc, transform);
		this.timeoutInSeconds = timeoutInSeconds;
		this.xClientInfo = xClientInfo;
		this.asyncProc = asyncProc;
		this.useCache = useCache;

	}

	public void draw(final Graphics2D g2d, final MapContent mapContent,
			final MapViewport mapViewport) {

		latch = new CountDownLatch(fc.size());

		Rectangle rect = mapViewport.getScreenArea();

		BufferedImage bi = new BufferedImage(rect.width, rect.height,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = (Graphics2D) bi.getGraphics();

		try {

			try {
				FeatureIterator<SimpleFeature> iterator = fc.features();
				try {

					for (; iterator.hasNext();) {
						final SimpleFeature f = iterator.next();

						processTileFeature(g, f, mapContent, mapViewport);
					}

				} finally {
					iterator.close();

				}
			} finally {
				latch.await(timeoutInSeconds, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {

		} finally {
			float alpha = (getLayerOpacity()) / 100f;

			int rule = AlphaComposite.SRC_OVER;
			g2d.setComposite(AlphaComposite.getInstance(rule, alpha));

			g2d.drawImage(bi, 0, 0, null);
			g.dispose();
			bi.flush();

		}

	}

	public void drawBlobFeature(byte[] blob, final SimpleFeature f,
			final Graphics2D g2d, final MapContent mapContent,
			final MapViewport mapViewport) throws IOException {

		ByteArrayInputStream bis = new ByteArrayInputStream(blob);
		BufferedImage imageBuf;
		imageBuf = ImageIO.read(bis);

		if (imageBuf != null) {
			drawImageFeature(g2d, mapContent, f, imageBuf);
		} else {
			throw new IOException("CACHED BLOB FAILURE");
		}

		imageBuf.flush();

	}

	public void drawFeature(final SimpleFeature f, final String urlStr,
			final Graphics2D g2d, final MapContent mapContent,
			final MapViewport mapViewport, final boolean isCacheable)
			throws MalformedURLException {

		final URL url = new URL(urlStr);
		final HttpGet request = new HttpGet(urlStr);

		String credentials = (String) f.getProperty("credentials").getValue();
		if (credentials != null) {
			request.setHeader("Host", url.getHost());
			request.setHeader("Authorization", credentials);
		}

		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1) paikkatietoikkuna.fi/printout");
		request.setHeader("Referer", "http://www.paikkatietoikkuna.fi/");
		if (xClientInfo != null) {
			request.setHeader("X-TileRequest-Forwarded-For", xClientInfo);
		}

		FutureCallback<HttpResponse> drawFuture = new FutureCallback<HttpResponse>() {

			public void cancelled() {
				log.warn("CANCELLED " + url.toExternalForm());
				latch.countDown();
			}

			public void completed(final HttpResponse response) {
				boolean inSuccessStatusRange = ((response.getStatusLine()
						.getStatusCode() / 200) == 1);

				try {

					if (inSuccessStatusRange) {
						BufferedImage imageBuf = ImageIO.read(response
								.getEntity().getContent());

						drawImageFeature(g2d, mapContent, f, imageBuf);

						if (isCacheable) {
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							ImageIO.write(imageBuf, "png", bos);
							JedisCache.getBlobCache().putToCache(
									urlStr.getBytes(), bos.toByteArray());
						}

						imageBuf.flush();

					} else {
						log.info(" CODE #"
								+ (inSuccessStatusRange ? "YEP" : "NOP"));
					}
				} catch (IllegalStateException e) {

					log.warn(e);
				} catch (IOException e) {

					log.warn(e);
				} finally {
					latch.countDown();
				}

			}

			public void failed(final Exception ex) {
				log.warn("FAILED " + url.toExternalForm() + ex);
				latch.countDown();
			}

		};

		asyncProc.execute(request, drawFuture);
	}

	private void processTileFeature(Graphics2D g, SimpleFeature f,
			final MapContent mapContent, final MapViewport mapViewport) {
		final String url = (String) f.getProperty("url").getValue();

		final Boolean isCacheable = (Boolean) f.getProperty("cacheable")
				.getValue() && useCache;

		byte[] blob = null;

		if (isCacheable) {
			blob = JedisCache.getBlobCache().getFromCache(url.getBytes());
		}

		if (blob != null) {

			try {
				drawBlobFeature(blob, f, g, mapContent, mapViewport);
				latch.countDown();
			} catch (IOException io) {
				try {
					drawFeature(f, url, g, mapContent, mapViewport, isCacheable);
				} catch (MalformedURLException e) {
					log.warn("Draw Feature failed " + e);
				}
			}

		} else if (SupportedDataUrlFormat.JPEG_Base64.match(url)) {

			blob = SupportedDataUrlFormat.JPEG_Base64.parse(url);

			try {
				drawBlobFeature(blob, f, g, mapContent, mapViewport);
				latch.countDown();
			} catch (IOException e) {
				log.warn("Draw Base64 JPEG Feature failed " + e + "\n" + url);
			}

		} else if (SupportedDataUrlFormat.PNG_Base64.match(url)) {

			blob = SupportedDataUrlFormat.PNG_Base64.parse(url);
			try {
				drawBlobFeature(blob, f, g, mapContent, mapViewport);
				latch.countDown();
			} catch (IOException e) {
				log.warn("Draw Base64 PNG Feature failed " + e + "\n" + url);
			}

		} else {
			try {
				drawFeature(f, url, g, mapContent, mapViewport, isCacheable);
			} catch (MalformedURLException e) {
				log.warn("Draw Feature failed " + e);

			}
		}

	}

}
