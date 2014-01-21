package fi.nls.oskari.printout.ws.jaxrs.resource;

enum PostJsonTestFileType {

	GEOJSON("%1$s.json", "application/json"), JSON("%1$s.json",
			"application/json"), PNG(
			"test-output/webservice_post_json-%1$s.png", "image/png"), PDF(
			"test-output/webservice_post_json-%1$s.pdf", "application/pdf"), PPTX(
			"test-output/webservice_post_json-%1$s.pptx",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation"), DOCX(
			"test-output/webservice_post_json-%1$s.docx",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document");

	private String mimeType;
	private String filenameTemplate;

	PostJsonTestFileType(final String template, final String mimeType) {
		filenameTemplate = template;
		this.mimeType = mimeType;
	}

	String getFilename(Object... strings) {
		return String.format(filenameTemplate, strings);
	}

	public String getMimeType() {
		return mimeType;
	}

}
