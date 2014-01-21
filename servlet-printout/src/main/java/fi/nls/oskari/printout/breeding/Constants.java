package fi.nls.oskari.printout.breeding;

public interface Constants {
	
	enum BreedRequestType {
		GEORSS, SINGLE_GEOMETRY, MULTI_GEOMETRY, NLSFI_PREDEFINED_DATASET
	}

	enum Status {
		INITIAL, SELECTED, QUEUED, BUSY, PAUSED, FINISHED, STOPPED, CANCELLED, FAILED
	}

}
