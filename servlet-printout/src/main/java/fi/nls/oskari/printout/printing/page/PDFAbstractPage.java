package fi.nls.oskari.printout.printing.page;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import fi.nls.oskari.printout.input.content.*;
import fi.nls.oskari.printout.input.content.PrintoutContentStyle.ColourStyleAttr;
import fi.nls.oskari.printout.input.content.PrintoutContentStyle.MetricsStyleAttr;
import fi.nls.oskari.printout.input.content.PrintoutContentTable.Col;
import fi.nls.oskari.printout.input.content.PrintoutContentTable.Row;
import fi.nls.oskari.printout.printing.PDFProducer.Options;
import fi.nls.oskari.printout.printing.PDFProducer.Page;
import fi.nls.oskari.printout.printing.PDFProducer.PageCounter;
import fi.nls.oskari.printout.printing.PDPageContentStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentProperties;
import org.apache.pdfbox.pdmodel.markedcontent.PDPropertyList;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

//import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

public class PDFAbstractPage {

	protected Page page;
	protected Options opts;
	protected PDFont font;

	public PDFAbstractPage(Page page, Options opts, PDFont font) {
		this.page = page;
		this.opts = opts;
		this.font = font;
	}

	/*
	 * shall move to use transforms some day
	 */
	protected void createTextAt(PDPageContentStream contentStream, String text,
			float xcm, float ycm, float fontPtSize, int r, int g, int b)
			throws IOException {

		float f[] = { xcm, ycm };
		page.getTransform().transform(f, 0, f, 0, 1);

		float fontSize = fontPtSize / 72f * 2.54f;
		contentStream.setFont(font, fontSize);
		contentStream.beginText();
		AffineTransform rowMatrix = new AffineTransform(page.getTransform());
		rowMatrix.translate(xcm, ycm);
		contentStream.setTextMatrix(rowMatrix);

		contentStream.setNonStrokingColor(r, g, b);
		contentStream.drawString(text);
		contentStream.endText();

	}

	protected BufferedImage doScaleWithFilters(BufferedImage image, int width,
			int height) throws IOException {

		final ResampleOp resampleOp = new ResampleOp(width, height);
		resampleOp.setNumberOfThreads(2);
		ResampleFilter filter = ResampleFilters.getLanczos3Filter();
		resampleOp.setFilter(filter);

		BufferedImage scaledImage = resampleOp.filter(image, null);

		return scaledImage;

	}

	protected void createContentOverlay(PDDocument targetDoc,
			PDPageContentStream contentStream,
			PDOptionalContentProperties ocprops, PDPropertyList props,
			PrintoutContent content, PageCounter pageCounter)
			throws IOException {
		if (content == null || ( content.getParts().isEmpty() && content.getPages().isEmpty() ) ) {
			return;
		}

		PDOptionalContentGroup tablesGroup = new PDOptionalContentGroup(
				"content"+pageCounter.getPage());
		ocprops.addGroup(tablesGroup);

		COSName mc0 = COSName.getPDFName("Mcontent"+pageCounter.getPage());
		props.putMapping(mc0, tablesGroup);

		contentStream.beginMarkedContentSequence(COSName.OC, mc0);

		contentStream.saveGraphicsState();

		/* content for all pages */
		createContentForPage(targetDoc, contentStream, content.getParts(),
				pageCounter);

		/* content for this page */
		final PrintoutContentPage pageContent = pageCounter.getPage() < opts
				.getContent().getPages().size() ? opts.getContent().getPages()
				.get(pageCounter.getPage()) : null;
		if (pageContent != null) {
			final List<PrintoutContentPart> pageContentParts = pageContent
					.getParts();
			createContentForPage(targetDoc, contentStream, pageContentParts,
					pageCounter);
		}

		contentStream.restoreGraphicsState();

		contentStream.endMarkedContentSequence();

	}

	protected void createContentForPage(PDDocument targetDoc,
			PDPageContentStream contentStream,
			final List<PrintoutContentPart> parts, PageCounter pageCounter)
			throws IOException {
		if (parts == null) {
			return;
		}
		for (PrintoutContentPart part : parts) {

			switch (part.getType()) {
			case table: {

				PrintoutContentTable table = (PrintoutContentTable) part;

				PrintoutContentStyle style = table.getStyle();

				/* Float width = style.getMetrics().get(MetricsStyleAttr.width); */
				Float height = style.getMetrics().get(MetricsStyleAttr.height);
				Float left = style.getMetrics().get(MetricsStyleAttr.left);
				Float bottom = style.getMetrics().get(MetricsStyleAttr.bottom);

				AffineTransform anchorTransform = new AffineTransform(
						page.getTransform());

				anchorTransform.translate(left, bottom);
				anchorTransform.translate(0, height);

				/* headers */
				/*
				 * for (Col col : table.getCols()) { }
				 */

				/* cells */

				int r = 0;
				for (Row row : table.getRows()) {

					int c = 0;
					for (Col col : table.getCols()) {

						PrintoutContentStyle colStyle = col.getStyle();

						String cellValue = (String) row.getValue(col);
						if (cellValue == null) {
							continue;
						}

						Float xcm = colStyle.getMetrics().get(
								MetricsStyleAttr.width);
						if (xcm == null) {
							xcm = 5f;
						}
						Float ycm = colStyle.getMetrics().get(
								MetricsStyleAttr.height);
						if (ycm == null) {
							ycm = 1f;
						}

						AffineTransform rowTransform = new AffineTransform(
								anchorTransform);
						rowTransform.translate(xcm * c, ycm * -r);

						float fontSize = 10 / 72f * 2.54f;
						contentStream.setFont(font, fontSize);

						/*
						 * Color backgroundColor = colStyle.getColours().get(
						 * ColourStyleAttr.backgroundColor);
						 */

						Color color = colStyle.getColours().get(
								ColourStyleAttr.color);
						if (color == null) {
							color = Color.black;
						}
						contentStream.beginText();
						contentStream.setTextMatrix(rowTransform);
						contentStream.setNonStrokingColor(color);

						contentStream.drawString(cellValue);
						contentStream.endText();

						Color borderColor = colStyle.getColours().get(
								ColourStyleAttr.borderColor);

						if (borderColor != null) {

						}

						c++;
					}

					r++;

				}

				break;
			}
			default:
				break;

			}

		}

	}

}
