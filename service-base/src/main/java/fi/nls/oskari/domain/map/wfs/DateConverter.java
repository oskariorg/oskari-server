package fi.nls.oskari.domain.map.wfs;

public class DateConverter extends Converter {
	
	protected DateConverter() {		
	}
	
	@Override
	public String toString() {
		return "DATE_CONVERTER";
	}
	
	@Override
	public String getConvertedValue() {
		return rawValue;
	}
}
