package fi.nls.oskari.printout.printing.page;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import fi.nls.oskari.printout.printing.PDFProducer.Options;
import fi.nls.oskari.printout.printing.PDFProducer.Page;

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
			float xcm, float ycm, float fontSize, int r, int g,
			int b) throws IOException {

		createTextAtTarget(contentStream, text, xcm / 2.54f * 72f,
				ycm / 2.54f * 72f, fontSize, r, g, b);

	}

	protected void createTextAtTarget(PDPageContentStream contentStream,
			String text, float xcm, float ycm, float fontSize,
			int r, int g, int b) throws IOException {
		contentStream.beginText();
		contentStream.setFont(font, fontSize);
		contentStream.setNonStrokingColor(r, g, b);
		contentStream.moveTextPositionByAmount(xcm, ycm);
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

}
