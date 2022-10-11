package org.oskari.component;

import fi.nls.oskari.service.ServiceRuntimeException;

public class ComponentSkippedRuntimeException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ComponentSkippedRuntimeException(final String message, final Throwable e) {
        super(message, e);
    }

    public ComponentSkippedRuntimeException(final String message) {
        super(message);
    }
}
