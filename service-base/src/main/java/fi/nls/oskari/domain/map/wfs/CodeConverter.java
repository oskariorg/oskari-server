package fi.nls.oskari.domain.map.wfs;

/**
 * Convert value according to an existing value map. The value map is located e.g. in a database.
 */
public class CodeConverter extends Converter {
	public static final int CODE_TYPE_PAIKKA_ID = 0;
	public static final int CODE_TYPE_PAIKKATYYPPI = 1;
	
	private int codeType;
	
	protected CodeConverter() {
	}
	
	@Override
	public String toString() {
		return "CODE_CONVERTER";
	}
	
	@Override
	public String getConvertedValue() {
		// TODO: convert value according to the code type variable
		return rawValue;
	}
	
	public int getCodeType() {
		return codeType;
	}
	public void setCodeType(int codeType) {
		this.codeType = codeType;
	}
}
