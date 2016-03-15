package fi.nls.oskari.printout.printing.page;

import fi.nls.oskari.printout.printing.PDFProducer.PageCounter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;

public interface PDFPage {
	public void createPages(PDDocument targetDoc, PageCounter pageCounter) throws IOException,
			TransformException;
}
