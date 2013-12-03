package fi.nls.oskari.domain.map.wfs;

public class NoneConverter extends Converter {
	
	protected NoneConverter() {
	}
	
	@Override
	public String toString() {
		return "NONE_CONVERTER";
	}
	
	@Override
	public String getConvertedValue() {
		return rawValue;
	}
}
