package fi.nls.oskari.printout.input.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.printout.input.content.PrintoutContentStyle.ColourStyleAttr;
import fi.nls.oskari.printout.input.content.PrintoutContentStyle.MetricsStyleAttr;
import fi.nls.oskari.printout.input.content.PrintoutContentTable.Col;
import fi.nls.oskari.printout.input.content.PrintoutContentTable.Row;

public class PrintoutContentParser {

	/*
	 * 
	 * "printout": { "styles": { "table1": { "width" : "20cm", "height": "5cm",
	 * "bottom": "15cm", "left": "1cm", "cols" : [{ "id": "col1", "width":
	 * "5cm", "height": "1cm" },{ "id": "col2", "width": "5cm", "height": "1cm"
	 * },{ "id": "col3", "width": "5cm", "height": "1cm" }]
	 * 
	 * } }, "content": { "table1" : { "type" : "table", "header": [ ], "rows" :
	 * [ { "col1": "1.1", "col2": "1.2", "col3": "1.3" }, { "col1": "2.1",
	 * "col2": "2.2", "col3": "2.3" }, { "col1": "3.1", "col2": "3.2", "col3":
	 * "3.3" }, { "col1": "4.1", "col2": "4.2", "col3": "4.3" } ], "footer" : [
	 * ]
	 * 
	 * }
	 * 
	 * 
	 * }
	 * 
	 * }
	 */

	@SuppressWarnings("unchecked")
	void parseStyle(PrintoutContentStyle style, final Map<String, ?> obj) {

		for (MetricsStyleAttr attr : PrintoutContentStyle.MetricsStyleAttr
				.values()) {

			style.getMetrics().put(attr,
					attr.parseStyleAttr((String) obj.get(attr.toString())));

		}

		for (ColourStyleAttr attr : PrintoutContentStyle.ColourStyleAttr
				.values()) {

			style.getColours().put(attr,
					attr.parseStyleAttr((String) obj.get(attr.toString())));
		}

		if (obj.get("styles") != null) {
			Map<String, PrintoutContentStyle> styles = new HashMap<String, PrintoutContentStyle>();
			parseStyles(styles, (Map<String, ?>) obj.get("styles"));
			style.setStyles(styles);
		}

	}

	@SuppressWarnings("unchecked")
	void parseStyles(Map<String, PrintoutContentStyle> styles,
			final Map<String, ?> obj) {
		if (obj == null) {
			return;
		}

		for (String key : obj.keySet()) {
			PrintoutContentStyle style = new PrintoutContentStyle();
			parseStyle(style, (Map<String, ?>) obj.get(key));

			styles.put(key, style);
		}

	}

	@SuppressWarnings("unchecked")
	void parseContent(Map<String, PrintoutContentStyle> styles,
			List<PrintoutContentPart> parts, final Map<String, ?> obj) {
		if (obj == null) {
			return;
		}

		for (String key : obj.keySet()) {

			Map<String, ?> contentPartObj = (Map<String, ?>) obj.get(key);

			PrintoutContentPartType type = PrintoutContentPartType
					.valueOf((String) contentPartObj.get("type"));

			String styleRef = (String) contentPartObj.get("style");

			switch (type) {
			case table:
				PrintoutContentTable table = new PrintoutContentTable();
				parseTable(table, contentPartObj);
				if (styleRef != null) {
					table.setStyle(styles.get(styleRef));
				}
				parts.add(table);
				break;
			default:
				break;

			}

		}
	}

	enum TableParts {
		cols, rows, header, footer;

	}

	@SuppressWarnings("unchecked")
	private void parseTable(PrintoutContentTable table,
			Map<String, ?> contentPartObj) {

		for (TableParts tableDefPart : TableParts.values()) {

			switch (tableDefPart) {
			case cols:
				List<Map<String, ?>> colDefs = (List<Map<String, ?>>) contentPartObj
						.get(tableDefPart.toString());
				for (Map<String, ?> colDef : colDefs) {

					Col col = table.createCol();
					col.setId((String) colDef.get("id"));
				}
				break;
			case footer:
				break;
			case header:
				break;
			case rows:
				List<Map<String, ?>> rowDatas = (List<Map<String, ?>>) contentPartObj
						.get(tableDefPart.toString());

				for (Map<String, ?> rowData : rowDatas) {

					Row row = table.createRow();
					row.setValues(rowData);
				}

				break;
			default:
				break;

			}

		}

	}

	@SuppressWarnings("unchecked")
	public PrintoutContent parse(final Map<String, ?> obj) {
		if (obj == null) {
			return null;
		}
		PrintoutContent content = new PrintoutContent();

		parseStyles(content.getStyles(), (Map<String, ?>) obj.get("styles"));
		parseContent(content.getStyles(), content.getParts(),
				(Map<String, ?>) obj.get("content"));
		parsePages(content.getStyles(), content.getPages(),
				(List<Map<String, ?>>) obj.get("pages"));

		return content;
	}

	@SuppressWarnings("unchecked")
	private void parsePages(Map<String, PrintoutContentStyle> styles,
			List<PrintoutContentPage> pages, List<Map<String, ?>> list) {
		if (list == null) {
			return;
		}

		for (Map<String, ?> pageObj : list) {

			/* String styleRef = (String) pageObj.get("style"); */

			PrintoutContentPage page = new PrintoutContentPage();

			parseContent(styles, page.getParts(),
					(Map<String, ?>) pageObj.get("content"));

			pages.add(page);
		}

	}

}
