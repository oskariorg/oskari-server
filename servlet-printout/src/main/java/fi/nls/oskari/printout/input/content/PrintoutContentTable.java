package fi.nls.oskari.printout.input.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrintoutContentTable implements PrintoutContentPart {

	final List<Col> cols = new ArrayList<Col>();
	final List<Row> rows = new ArrayList<Row>();

	private PrintoutContentStyle style;

	public class Col {
		String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public PrintoutContentStyle getStyle() {
			if (style == null) {
				return style;
			}
			Map<String, PrintoutContentStyle> colStyles = style.getStyles();
			if (colStyles == null)
				return null;

			return colStyles.get(id);
		}
	}

	public class Row {

		Map<String, ?> values;

		public Map<String, ?> getValues() {
			return values;
		}

		public void setValues(Map<String, ?> values) {
			this.values = values;
		}

		public Object getValue(Col col) {
			return getValues().get(col.getId());
		}

	}

	public void setStyle(final PrintoutContentStyle style) {
		this.style = style;

	}

	public List<Col> getCols() {
		return cols;
	}

	public Col createCol() {
		Col col = new Col();
		getCols().add(col);
		return col;
	}

	public List<Row> getRows() {
		return rows;
	}

	public Row createRow() {
		Row row = new Row();
		getRows().add(row);
		return row;
	}

	
	public PrintoutContentPartType getType() {
		return PrintoutContentPartType.table;
	}

	
	public PrintoutContentStyle getStyle() {
		return style;
	}

}
