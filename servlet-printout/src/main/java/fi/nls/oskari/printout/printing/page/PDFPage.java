package fi.nls.oskari.printout.printing.page;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.opengis.referencing.operation.TransformException;

import fi.nls.oskari.printout.printing.PDFProducer.PageCounter;

public interface PDFPage {
	public void createPages(PDDocument targetDoc, PageCounter pageCounter) throws IOException,
			TransformException;
}
