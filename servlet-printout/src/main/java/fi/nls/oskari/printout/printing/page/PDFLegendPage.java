package fi.nls.oskari.printout.printing.page;

import fi.nls.oskari.printout.printing.PDFProducer.Options;
import fi.nls.oskari.printout.printing.PDFProducer.Page;
import fi.nls.oskari.printout.printing.PDFProducer.PageCounter;
import fi.nls.oskari.printout.printing.PDPageContentStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.markedcontent.PDPropertyList;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
/*import org.apache.pdfbox.pdmodel.font.PDType1Font;*/

/**
 * this class adds map legend page. W-i-P as some map legend images span
 * multiple pages in length.
 * 
 */
public class PDFLegendPage extends PDFAbstractPage implements PDFPage {

	class LegendImage {
		PDXObjectImage ximage;
		int w;
		int h;

	}

	public PDFLegendPage(Page page, Options opts, PDFont font) {
		super(page, opts, font);

	}

	public void createPages(PDDocument targetDoc, PageCounter pageCounter)
			throws IOException, TransformException {
		PDDocumentCatalog catalog = targetDoc.getDocumentCatalog();

		PDPage targetPage = page.createNewPage(targetDoc,
				opts.getPageTemplate() != null, pageCounter);

		PDResources resources = targetPage.findResources();
		if (resources == null) {
			resources = new PDResources();
			targetPage.setResources(resources);
		}

		PDOptionalContentProperties ocprops = catalog.getOCProperties();

		if (ocprops == null) {
			ocprops = new PDOptionalContentProperties();
			catalog.setOCProperties(ocprops);
		}

		PDPropertyList props = new PDPropertyList();
		resources.setProperties(props);

		PDPageContentStream contentStream = page.createContentStreamTo(
				targetDoc, targetPage, opts.getPageTemplate() != null);

		createTextLayerOverlay(targetDoc, contentStream, ocprops, props);
		
		contentStream.close();

	}

	void createTextLayerOverlay(PDDocument targetDoc,
			PDPageContentStream contentStream,
			PDOptionalContentProperties ocprops, PDPropertyList props)
			throws IOException, TransformException {

		float logoWidth = 16;
		float logoHeight = 16;

		PDXObjectImage xlogo = null;

		if (opts.isPageLogo()) {
			/* MUST create before optiona content group is created */
			/*
			 * - this is a googled fix to not being able to show images in
			 * overlays
			 */
			InputStream inp = getClass().getResourceAsStream("logo.png");
			try {
				BufferedImage imageBuf = ImageIO.read(inp);
				int w = imageBuf.getWidth(null);
				int h = imageBuf.getHeight(null);
				BufferedImage bi = new BufferedImage(w, h,
						BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = (Graphics2D) bi.getGraphics();
				g.drawImage(imageBuf, 0, 0, null);
				g.dispose();

				bi = doScaleWithFilters(bi, (int) logoWidth * 4,
						(int) logoHeight * 4);

				xlogo = new PDPixelMap(targetDoc, bi);
			} finally {
				inp.close();
			}
		}

		/*
		 * { URL url = new URL(
		 * "http://xml.nls.fi/Rasteriaineistot/Merkkienselitykset/2010/01/peruskartta_mk25000.png"
		 * );
		 * 
		 * InputStream inp = url.openStream(); try { BufferedImage imageBuf =
		 * ImageIO.read(inp); int w = imageBuf.getWidth(null); int h =
		 * imageBuf.getHeight(null);
		 * 
		 * LegendImage limage = new LegendImage(); limage.ximage = new
		 * PDPixelMap(targetDoc, imageBuf); limage.w = w; limage.h = h;
		 * 
		 * legends.add( limage ); } finally { inp.close(); } }
		 */

		PDOptionalContentGroup layerGroup = new PDOptionalContentGroup("legend");
		ocprops.addGroup(layerGroup);

		COSName mc0 = COSName.getPDFName("MClegend");
		props.putMapping(mc0, layerGroup);
		/* PDFont font = PDType1Font.HELVETICA_BOLD; */
		contentStream.beginMarkedContentSequence(COSName.OC, mc0);

		/* BEGIN overlay content */

		/* title */

		if (opts.getPageTitle() != null) {
			String pageTitle = StringEscapeUtils.unescapeHtml4(Jsoup.clean(
					opts.getPageTitle(), Whitelist.simpleText()));

			createTextAt(contentStream, pageTitle, 9.0f, page.getHeight() - 1f,
					opts.getFontSize(), 0, 0, 0);

		}

		/* pvm */
		if (opts.isPageDate()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Locale l = new Locale("fi");
			Date dte = Calendar.getInstance(l).getTime();

			String dateStr = sdf.format(dte);

			createTextAt(contentStream, dateStr, page.getWidth() - 4f,
					page.getHeight() - 1f, opts.getFontSize(), 0, 0, 0);

		}

		/* logo */
		if (opts.isPageLogo()) {
			contentStream.setNonStrokingColor(255, 255, 255);
			contentStream.setStrokingColor(255, 255, 255);

			contentStream.drawXObject(xlogo, 1.0f / 2.54f * 72f, 16, logoWidth,
					logoHeight);

		}

		/* END overlay content */

		contentStream.endMarkedContentSequence();

		contentStream.close();

	}

}
