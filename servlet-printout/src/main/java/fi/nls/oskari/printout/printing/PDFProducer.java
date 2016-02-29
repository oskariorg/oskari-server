package fi.nls.oskari.printout.printing;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.printout.input.content.PrintoutContent;
import fi.nls.oskari.printout.printing.page.PDFContentPage;
import fi.nls.oskari.printout.printing.page.PDFLayeredImagesPage;
import fi.nls.oskari.printout.printing.page.PDFLegendPage;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.xml.transform.TransformerException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.List;

//import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

/**
 * This class produces a basic PDF document.
 * 
 * @todo fix to use transforms instead of manual cm to pixel mappings
 * 
 */
public class PDFProducer {

	public static class Options {
		boolean pageDate = false;
		boolean pageScale = false;
		boolean pageLogo = false;
		String pageLogoResource = "logo.png";
		boolean pageLegend = false;
		boolean pageCopyleft = false;
		Float[] pageMapRect = null;
		String pageTitle;
		String pageTemplate;
		float fontSize = 12f;
		PrintoutContent content = null;

		public float getFontSize() {
			return fontSize;
		}

		public String getPageTitle() {
			return pageTitle;
		}

		public boolean isPageCopyleft() {
			return pageCopyleft;
		}

		public boolean isPageDate() {
			return pageDate;
		}

		public boolean isPageLegend() {
			return pageLegend;
		}

		public boolean isPageLogo() {
			return pageLogo;
		}

		public boolean isPageScale() {
			return pageScale;
		}

		public void setFontSize(float fontSize) {
			this.fontSize = fontSize;
		}

		public void setPageCopyleft(boolean pageCopyleft) {
			this.pageCopyleft = pageCopyleft;
		}

		public void setPageDate(boolean pageDate) {
			this.pageDate = pageDate;
		}

		public void setPageLegend(boolean pageLegend) {
			this.pageLegend = pageLegend;
		}

		public void setPageLogo(boolean pageLogo) {
			this.pageLogo = pageLogo;
		}

		public void setPageScale(boolean pageScale) {
			this.pageScale = pageScale;
		}

		public void setPageTitle(String pageTitle) {
			this.pageTitle = pageTitle;
		}

		public String getPageTemplate() {
			return pageTemplate;
		}

		public void setPageTemplate(String pageTemplate) {
			this.pageTemplate = pageTemplate;
		}

		public Float[] getPageMapRect() {
			return pageMapRect;
		}

		public void setPageMapRectFromString(String pmrString) {
			if (pmrString == null) {
				return;
			}
			String[] parts = pmrString.split(",");

			float mapLeft = Float.valueOf(parts[0]);
			float mapBottom = Float.valueOf(parts[1]);
			float mapWidth = Float.valueOf(parts[2]);
			float mapHeight = Float.valueOf(parts[3]);

			pageMapRect = new Float[] { mapLeft, mapBottom, mapWidth, mapHeight };

		}

		public PrintoutContent getContent() {
			return content;
		}

		public void setContent(PrintoutContent content) {
			this.content = content;
		}

        public String getPageLogoResource() {
            return pageLogoResource;
        }

        public void setPageLogoResource(String pageLogoResource) {
            this.pageLogoResource = pageLogoResource;
        }
		
		

	};

	public enum Page {
		/* A4 */
		A4(PDPage.PAGE_SIZE_A4, -1, Double.valueOf((20.9 - 1 * 2) / 2.54 * 72)
				.intValue(), Double.valueOf((int) (29.7 - 1.5 * 2) / 2.54 * 72)
				.intValue()),
		/* A3 */
		A3(PDPage.PAGE_SIZE_A3, -1, Double.valueOf((29.7 - 1 * 2) / 2.54 * 72)
				.intValue(), Double.valueOf((42.0 - 1.5 * 2) / 2.54 * 72)
				.intValue()),
		/* A4 landscape */
		A4_Landscape(PDPage.PAGE_SIZE_A4, 90, Double.valueOf(
				(29.7 - 1 * 2) / 2.54 * 72).intValue(), Double.valueOf(
				(20.9 - 1.5 * 2) / 2.54 * 72).intValue()),
		/* A3 landscape */
		A3_Landscape(PDPage.PAGE_SIZE_A3, 90, Double.valueOf(
				(42.0 - 1 * 2) / 2.54 * 72).intValue(), Double.valueOf(
				(29.7 - 1.5 * 2) / 2.54 * 72).intValue())
		/* end of list */
		;

		final private PDRectangle rect;
		final private int degrees;
		final private AffineTransform transform = new AffineTransform();
		final private int widthTarget;
		final private int heightTarget;

		Page(PDRectangle pageRect, int degrees, int w, int h) {
			rect = pageRect;
			this.degrees = degrees;
			transform.scale(72.0 / 2.54, 72.0 / 2.54);
			this.widthTarget = w;
			this.heightTarget = h;
		}

		public PDPageContentStream createContentStreamTo(PDDocument targetDoc,
				PDPage targetPage, boolean append) throws IOException {

			PDPageContentStream contentStream = append ? new PDPageContentStream(
					targetDoc, targetPage, true, true, true)
					: new PDPageContentStream(targetDoc, targetPage, false,
							false);
			contentStream.setResources(targetPage.findResources());
			PDRectangle pageSize = targetPage.findMediaBox();
			float pageWidth = pageSize.getWidth();
			if (degrees != -1) {
				contentStream.concatenate2CTM(0, 1, -1, 0, pageWidth, 0);
			}

			return contentStream;
		}

		public PDPage createNewPage(PDDocument doc, boolean useTemplate,
				PageCounter pageCounter) {
			PDPage page = null;
			if (useTemplate) {
				/* let's assume we have a template page for each page */
				page =  (PDPage) doc.getDocumentCatalog().getAllPages()
						.get(pageCounter.nextPage());


			} else {
				page = new PDPage();
				page.setMediaBox(rect);
				page.setTrimBox(rect);
				page.setBleedBox(rect);

				doc.addPage(page);
				pageCounter.nextPage();

			}
            if (degrees != -1) {
                page.setRotation(degrees);
            }
			return page;

		}

		public float getWidth() {
			if (degrees == -1) {
				return rect.getWidth() / 72f * 2.54f;
			} else {
				return rect.getHeight() / 72f * 2.54f;
			}
		}

		public float getHeight() {
			if (degrees == -1) {
				return rect.getHeight() / 72f * 2.54f;
			} else {
				return rect.getWidth() / 72f * 2.54f;
			}
		}

		public int getMapWidthTargetInPoints(Options opts) {
			if (opts == null) {
				return widthTarget;
			}
			Float[] pageMapRect = opts.getPageMapRect();
			if (pageMapRect == null) {
				return widthTarget;
			} else {
				return new Float(72f * pageMapRect[2] / 2.54f).intValue();

			}
		}

		public int getMapHeightTargetInPoints(Options opts) {
			if (opts == null) {
				return heightTarget;
			}
			Float[] pageMapRect = opts.getPageMapRect();
			if (pageMapRect == null) {
				return heightTarget;
			} else {
				return new Float(72f * pageMapRect[3] / 2.54f).intValue();
			}
		}

		public AffineTransform getTransform() {
			return transform;
		}

	}

	public class PageCounter {
		int page = -1;

		public int nextPage() {
			return ++page;
		}

		public int getPage() {
			return page;
		}

		public int getPage1Based() {
			return page + 1;
		}

	}

	Page page = Page.A4;

	Options opts;
	private CoordinateReferenceSystem crs;

	final private PageCounter pageCounter;

	public PDFProducer(Page page) throws IOException {
		this.page = page;
		this.opts = new Options();
		this.pageCounter = new PageCounter();
	}

	public PDFProducer(Page page, Options opts,
			CoordinateReferenceSystem coordinateReferenceSystem)
			throws IOException {

		this.page = page;
		this.opts = opts;
		this.crs = coordinateReferenceSystem;
		this.pageCounter = new PageCounter();
	}

	protected PDDocument createDoc() throws IOException {
		if (opts.getPageTemplate() != null) {

			if (opts.getPageTemplate().indexOf("/") != -1) {
				throw new IOException("invalid page template definition");
			}
			if (opts.getPageTemplate().indexOf("..") != -1) {
				throw new IOException("invalid page template definition");
			}

			URL url = getClass().getResource(opts.getPageTemplate());

			PDDocument doc = PDDocument.load(url);

			if (doc.isEncrypted()) {
				throw new IOException(
						"Encrypted documents are not supported as templates");
			}

			return doc;

		} else {
			return new PDDocument();
		}
	}

	public void createLayeredPDFFromImages(List<BufferedImage> images,
			OutputStream outputStream, Envelope env, Point centre)
			throws Exception {

		PDDocument targetDoc = createDoc();

		try {
			createLayeredPDFPages(targetDoc, images, env, centre);
			createMetadata(targetDoc);
			createIcc(targetDoc);
			shaveTemplatePages(targetDoc);

			targetDoc.save(outputStream);
		} finally {
			targetDoc.close();

		}
	}

	private void shaveTemplatePages(PDDocument targetDoc) {
		if (opts.getPageTemplate() == null) {
			return;
		}

		/* let's remove unnecessary template pages */
		if (!(targetDoc.getNumberOfPages() > pageCounter.getPage() + 1)) {
			return;
		}

		for (int n = targetDoc.getNumberOfPages() - 1; n > pageCounter
				.getPage(); n--) {
			targetDoc.removePage(n);
		}

	}

	/**
	 * Add an image to an existing PDF document.
	 * 
	 * @param inputFile
	 *            The input PDF to add the image to.
	 * @param image
	 *            The filename of the image to put in the PDF.
	 * @param outputFile
	 *            The file to write to the pdf to.
	 * @throws Exception
	 */
	public void createLayeredPDFFromImages(List<BufferedImage> images,
			String outputFile, Envelope env, Point centre) throws Exception {
		PDDocument targetDoc = createDoc();
		try {
			createLayeredPDFPages(targetDoc, images, env, centre);
			createMetadata(targetDoc);
			createIcc(targetDoc);

			shaveTemplatePages(targetDoc);

			File targetFile = new File(outputFile);
			targetDoc.save(targetFile.getAbsolutePath());

		} finally {
			targetDoc.close();

		}

	}

	void createLayeredPDFPages(PDDocument targetDoc,
			List<BufferedImage> images, Envelope env, Point centre)
			throws IOException, TransformException {

		InputStream fontStream = getClass().getResourceAsStream(
				"/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
		PDFont font = PDTrueTypeFont.loadTTF(targetDoc, fontStream);

		targetDoc.getDocument().setHeaderString("%PDF-1.5");
		PDDocumentCatalog catalog = targetDoc.getDocumentCatalog();
		catalog.setVersion("1.5");

		{
			PDFLayeredImagesPage pageImages = new PDFLayeredImagesPage(page,
					opts, font, crs, images, env, centre);

			pageImages.createPages(targetDoc, pageCounter);
		}

		if (opts.getContent() != null) {
			for (int n = pageCounter.getPage() + 1; n < opts.getContent()
					.getPages().size(); n++) {
				PDFContentPage contentPage = new PDFContentPage(page, opts,
						font);
				contentPage.createPages(targetDoc, pageCounter);
			}
		}

		if (opts.isPageLegend()) {

			PDFLegendPage pageLegend = new PDFLegendPage(page, opts, font);

			pageLegend.createPages(targetDoc, pageCounter);

		}

	}

	void createLayeredPDFPagesWithTemplate(PDDocument targetDoc,
			List<BufferedImage> images, int width, int height, Envelope env,
			Point centre, String templateResource) throws IOException,
			TransformException {

		InputStream fontStream = getClass().getResourceAsStream(
				"/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
		PDFont font = PDTrueTypeFont.loadTTF(targetDoc, fontStream);

		targetDoc.getDocument().setHeaderString("%PDF-1.5");
		PDDocumentCatalog catalog = targetDoc.getDocumentCatalog();
		catalog.setVersion("1.5");

		{
			PDFLayeredImagesPage pageImages = new PDFLayeredImagesPage(page,
					opts, font, crs, images, env, centre);

			pageImages.createPages(targetDoc, pageCounter);
		}

		if (opts.getContent() != null) {
			for (int n = pageCounter.getPage() + 1; n < opts.getContent()
					.getPages().size(); n++) {
				PDFContentPage contentPage = new PDFContentPage(page, opts,
						font);
				contentPage.createPages(targetDoc, pageCounter);
			}
		}

		if (opts.isPageLegend()) {

			PDFLegendPage pageLegend = new PDFLegendPage(page, opts, font);

			pageLegend.createPages(targetDoc, pageCounter);

		}

	}

	void createMetadata(PDDocument targetDoc) throws IOException,
			TransformerException {

		PDDocumentCatalog cat = targetDoc.getDocumentCatalog();
		PDMetadata metadataStream = new PDMetadata(targetDoc);
		cat.setMetadata(metadataStream);

		// jempbox version
		XMPMetadata metadata = new XMPMetadata();
		XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(metadata);
		metadata.addSchema(pdfaid);
		pdfaid.setConformance("B");
		pdfaid.setPart(1);
		pdfaid.setAbout("");

		XMPSchemaPDF pdfSchema = metadata.addPDFSchema();
		// pdfSchema.setKeywords( "" );
		pdfSchema.setProducer("oskari.org/printout-servlet");

		XMPSchemaBasic basicSchema = metadata.addBasicSchema();
		basicSchema.setModifyDate(new GregorianCalendar());
		basicSchema.setCreateDate(new GregorianCalendar());
		basicSchema.setCreatorTool("oskari.org/printout-servlet");
		basicSchema.setMetadataDate(new GregorianCalendar());

		XMPSchemaDublinCore dcSchema = metadata.addDublinCoreSchema();
		dcSchema.setTitle("oskari.org/printout-servlet");
		dcSchema.addCreator("oskari.org/printout-servlet");
		dcSchema.setDescription("");

		PDFContentMetadata ctSchema = new PDFContentMetadata(metadata);
		/*
		 * ctSchema.addCreator("Testing Proprietary metadata");
		 * ctSchema.addCreator("Testing Proprietary metadata values");
		 */
		if (opts.getContent() != null) {
			if (opts.getContent().getMetadata() != null) {

				ctSchema.setMetadataFromMap(opts.getContent().getMetadata());
			}
		}

		metadata.addSchema(ctSchema);

		metadataStream.importXMPMetadata(metadata);
		cat.setMetadata(metadataStream);

	}

	void createIcc(PDDocument targetDoc) throws Exception {

		// retrieve icc // this file cannot be added in PDFBox, it must be
		// downloaded
		// its localization is
		// http://www.color.org/sRGB_IEC61966-2-1_black_scaled.icc

		InputStream colorProfile = getClass().getResourceAsStream(
				"/org/color/sRGB_IEC61966-2-1_black_scaled.icc");
		try {

			PDOutputIntent oi = new PDOutputIntent(targetDoc, colorProfile);
			oi.setInfo("sRGB IEC61966-2.1");
			oi.setOutputCondition("sRGB IEC61966-2.1");
			oi.setOutputConditionIdentifier("sRGB IEC61966-2.1");
			oi.setRegistryName("http://www.color.org");

			PDDocumentCatalog cat = targetDoc.getDocumentCatalog();
			cat.addOutputIntent(oi);

		} finally {
			colorProfile.close();
		}
	}

}
