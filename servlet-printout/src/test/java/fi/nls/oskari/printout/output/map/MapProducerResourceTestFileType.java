package fi.nls.oskari.printout.output.map;

enum MapProducerResourceTestFileType {

	GEOJSON("%1$s.json"), JSON("%1$s.json"), PNG(
			"test-output/map_producer_resource-%1$s.png"), PDF(
			"test-output/map_producer_resource-%1$s.pdf"), PPTX(
			"test-output/map_producer_resource-%1$s.pptx");

	private String filenameTemplate;

	MapProducerResourceTestFileType(final String template) {
		filenameTemplate = template;
	}

	String getFilename(Object... strings) {
		return String.format(filenameTemplate, strings);
	}

}