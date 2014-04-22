package fi.nls.oskari.printout.printing.page;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
//import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
/*import org.apache.pdfbox.pdmodel.font.PDType1Font;*/
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.pdmodel.markedcontent.PDPropertyList;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.opengis.referencing.operation.TransformException;

import fi.nls.oskari.printout.printing.PDFProducer.Options;
import fi.nls.oskari.printout.printing.PDFProducer.Page;
import fi.nls.oskari.printout.printing.PDFProducer.PageCounter;
import fi.nls.oskari.printout.printing.PDPageContentStream;

/**
 * this class adds map legend page. W-i-P as some map legend images span
 * multiple pages in length.
 * 
 */
public class PDFContentPage extends PDFAbstractPage implements PDFPage {

	public PDFContentPage(Page page, Options opts, PDFont font) {
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

		createContentOverlay(targetDoc, contentStream, ocprops, props,
				opts.getContent(), pageCounter);

		contentStream.close();

	}

}
