package fi.nls.oskari.printout.printing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

import fi.nls.oskari.printout.printing.page.PDFLayeredImagesPage;
import fi.nls.oskari.printout.printing.page.PDFLegendPage;

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
		boolean pageLegend = false;
		boolean pageCopyleft = false;

		String pageTitle;

		float fontSize = 12f;

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
				(29.7 - 1.5 * 2) / 2.54 * 72).intValue())/*
														 * ,
														 * 
														 * A0(PDPage.PAGE_SIZE_A0
														 * , -1,
														 * Double.valueOf(33.1 *
														 * 72).intValue(),
														 * Double.valueOf(46.81
														 * * 72).intValue());
														 */

		/* end of list */
		;

		private PDRectangle rect;
		private int degrees;
		private int widthTarget;
		private int heightTarget;

		Page(PDRectangle pageRect, int rotation, int w, int h) {
			rect = pageRect;
			degrees = rotation;
			this.widthTarget = w;
			this.heightTarget = h;
		}

		public PDPageContentStream createContentStreamTo(PDDocument targetDoc,
				PDPage targetPage) throws IOException {
			PDPageContentStream contentStream = new PDPageContentStream(
					targetDoc, targetPage, false, false);
			PDRectangle pageSize = targetPage.findMediaBox();
			float pageWidth = pageSize.getWidth();
			if (degrees != -1) {
				contentStream.concatenate2CTM(0, 1, -1, 0, pageWidth, 0);
			}

			return contentStream;
		}

		public PDPage createNewPageTo(PDDocument doc) {
			PDPage page = new PDPage();
			page.setMediaBox(rect);
			page.setTrimBox(rect);
			page.setBleedBox(rect);
			if (degrees != -1) {
				page.setRotation(degrees);
			}
			doc.addPage(page);

			return page;
		}

		public int getHeightTargetInPoints() {
			return heightTarget;
		}

		public PDRectangle getRect() {
			return rect;
		}

		public int getWidthTargetInPoints() {
			return widthTarget;
		}
	}

	Page page = Page.A4;

	Options opts;
	private CoordinateReferenceSystem crs;

	public PDFProducer(Page page) throws IOException {
		this.page = page;
		this.opts = new Options();

	}

	public PDFProducer(Page page, Options opts,
			CoordinateReferenceSystem coordinateReferenceSystem)
			throws IOException {

		this.page = page;
		this.opts = opts;
		this.crs = coordinateReferenceSystem;
	}

	public void createLayeredPDFFromImages(List<BufferedImage> images,
			OutputStream outputStream, int width, int height, Envelope env,
			Point centre) throws Exception {

		PDDocument targetDoc = new PDDocument();
		try {
			createLayeredPDFPages(targetDoc, images, width, height, env, centre);
			createMetadata(targetDoc);
			createIcc(targetDoc);
			
			targetDoc.save(outputStream);
		} finally {
			targetDoc.close();

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
			String outputFile, int width, int height, Envelope env, Point centre)
			throws Exception {
		PDDocument targetDoc = new PDDocument();
		try {
			createLayeredPDFPages(targetDoc, images, width, height, env, centre);
			createMetadata(targetDoc);
			createIcc(targetDoc);

			File targetFile = new File(outputFile);
			targetDoc.save(targetFile.getAbsolutePath());

		} finally {
			targetDoc.close();

		}

	}

	void createLayeredPDFPages(PDDocument targetDoc,
			List<BufferedImage> images, int width, int height, Envelope env,
			Point centre) throws IOException, TransformException {

		InputStream fontStream = getClass().getResourceAsStream(
				"/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
		PDFont font = PDTrueTypeFont.loadTTF(targetDoc, fontStream);

		targetDoc.getDocument().setHeaderString("%PDF-1.5");
		PDDocumentCatalog catalog = targetDoc.getDocumentCatalog();
		catalog.setVersion("1.5");

		{
			PDFLayeredImagesPage pageImages = new PDFLayeredImagesPage(page,
					opts, font, crs, images, width, height, env, centre);

			pageImages.createPages(targetDoc);
		}

		if (opts.isPageLegend()) {

			PDFLegendPage pageLegend = new PDFLegendPage(page, opts, font);

			pageLegend.createPages(targetDoc);

		}

	}

	void createMetadata(PDDocument targetDoc) throws IOException,
			TransformerException {

		PDDocumentCatalog cat = targetDoc.getDocumentCatalog();
		PDMetadata metadata = new PDMetadata(targetDoc);
		cat.setMetadata(metadata);

		// jempbox version
		XMPMetadata xmp = new XMPMetadata();
		XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
		xmp.addSchema(pdfaid);
		pdfaid.setConformance("B");
		pdfaid.setPart(1);
		pdfaid.setAbout("");
		metadata.importXMPMetadata(xmp);

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
