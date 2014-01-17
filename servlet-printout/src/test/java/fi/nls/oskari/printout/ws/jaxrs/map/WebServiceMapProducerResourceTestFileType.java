package fi.nls.oskari.printout.ws.jaxrs.map;

enum WebServiceMapProducerResourceTestFileType {

	GEOJSON("%1$s.json"), JSON("%1$s.json"), PNG(
			"test-output/webservice_map_producer_resource-%1$s.png"), PDF(
			"test-output/webservice_map_producer_resource-%1$s.pdf"), PPTX(
			"test-output/webservice_map_producer_resource-%1$s.pptx")
	;

	private String filenameTemplate;

	WebServiceMapProducerResourceTestFileType(final String template) {
		filenameTemplate = template;
	}

	String getFilename(Object... strings) {
		return String.format(filenameTemplate, strings);
	}

}
