package fi.nls.oskari.printout.printing.page;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.opengis.referencing.operation.TransformException;

public interface PDFPage {
	public void createPages(PDDocument targetDoc) throws IOException,
			TransformException;
}
