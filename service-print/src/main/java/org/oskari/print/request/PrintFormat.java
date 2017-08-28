package org.oskari.print.request;

public enum PrintFormat {

    PDF("application/pdf", "pdf"),
    PNG("image/png", "png");

    public final String contentType;
    public final String fileExtension;

    private PrintFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public static PrintFormat getByContentType(String contentType) {
        if (contentType != null) {
            contentType = contentType.toLowerCase();
            for (PrintFormat f : values()) {
                if (f.contentType.equals(contentType)) {
                    return f;
                }
            }
        }
        return null;
    }

}
