package fi.nls.oskari.printout.printing.page;

import java.awt.geom.AffineTransform;
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
			float xcm, float ycm, float fontPtSize, int r, int g,
			int b) throws IOException {
		

		float f[] = { xcm, ycm };
		page.getTransform().transform(f, 0, f, 0, 1);
		
		float fontSize = fontPtSize / 72f * 2.54f;
		contentStream.setFont(font, fontSize);
		contentStream.beginText();
		AffineTransform rowMatrix = new AffineTransform(page.getTransform());
		rowMatrix.translate(xcm,ycm);
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

}
