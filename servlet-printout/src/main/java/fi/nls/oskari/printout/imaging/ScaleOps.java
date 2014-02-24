package fi.nls.oskari.printout.imaging;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

public class ScaleOps {
	public BufferedImage doScaleWithFilters(BufferedImage image, int width,
			int height) throws IOException {

		final ResampleOp resampleOp = new ResampleOp(width, height);
		resampleOp.setNumberOfThreads(2);
		ResampleFilter filter = ResampleFilters.getLanczos3Filter();
		resampleOp.setFilter(filter);

		BufferedImage scaledImage = resampleOp.filter(image, null);

		return scaledImage;

	}

	public BufferedImage doScaleBy2(BufferedImage image, int width, int height) throws IOException {
		return doScaleWithFilters(image,width,height);
	}
}
