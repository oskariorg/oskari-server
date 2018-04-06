package fi.mml.map.mapwindow.service.wms;

public class LayerNotFoundInCapabilitiesException extends Exception {

    private static final long serialVersionUID = 1L;

    public LayerNotFoundInCapabilitiesException(Exception e) {
        super(e);
    }

    public LayerNotFoundInCapabilitiesException(String s) {
        super(s);
    }

}
