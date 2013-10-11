package fi.nls.oskari.domain.map.wfs;

public abstract class Converter {
	public static final int CONVERTER_TYPE_CODE = 1;
	public static final int CONVERTER_TYPE_DATE = 2;
	public static final int CONVERTER_TYPE_NONE = 3;
	public static final int CONVERTER_TYPE_PROPERTY_MAP = 4;
	String rawValue = "";
	
	public static Converter getConverter(int converterId) {
		switch (converterId) {
			case CONVERTER_TYPE_CODE: return new CodeConverter();
			case CONVERTER_TYPE_DATE: return new DateConverter();
			case CONVERTER_TYPE_NONE: return new NoneConverter();
			case CONVERTER_TYPE_PROPERTY_MAP: return new PropertyMapConverter();
		}
		
		return new NoneConverter();
	}
	
	public String getRawValue() {
		return rawValue;
	}
	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
	}
	
	public abstract String getConvertedValue();
}
