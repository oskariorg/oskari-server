package fi.nls.oskari.domain.map.analysis;


import fi.nls.oskari.domain.map.UserDataLayer;

import java.lang.reflect.Method;
import java.util.List;


public class Analysis extends UserDataLayer {

    private long id;
    private String name;
    private long layer_id;
    private String analyse_json;
    private long style_id;
    private String col1;
    private String col2;
    private String col3;
    private String col4;
    private String col5;
    private String col6;
    private String col7;
    private String col8;
    private String col9;
    private String col10;
    private String select_to_data;
    private String override_sld;
    private long old_id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOld_id() {
        return old_id;
    }

    public void setOld_id(long old_id) {
        this.old_id = old_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLayer_id() {
        return layer_id;
    }

    public void setLayer_id(long layerId) {
        layer_id = layerId;
    }

    public String getAnalyse_json() {
        return analyse_json;
    }

    public void setAnalyse_json(String analyseJson) {
        analyse_json = analyseJson;
    }

    public long getStyle_id() {
        return style_id;
    }

    public void setStyle_id(long styleId) {
        style_id = styleId;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public String getCol3() {
        return col3;
    }

    public void setCol3(String col3) {
        this.col3 = col3;
    }

    public String getCol4() {
        return col4;
    }

    public void setCol4(String col4) {
        this.col4 = col4;
    }

    public String getCol5() {
        return col5;
    }

    public void setCol5(String col5) {
        this.col5 = col5;
    }

    public String getCol6() {
        return col6;
    }

    public void setCol6(String col6) {
        this.col6 = col6;
    }

    public String getCol7() {
        return col7;
    }

    public void setCol7(String col7) {
        this.col7 = col7;
    }

    public String getCol8() {
        return col8;
    }

    public void setCol8(String col8) {
        this.col8 = col8;
    }

    public String getCol9() {
        return col9;
    }

    public void setCol9(String col9) {
        this.col9 = col9;
    }

    public String getCol10() {
        return col10;
    }

    public void setCol10(String col10) {
        this.col10 = col10;
    }

    public String getSelect_to_data() {
        return select_to_data;
    }

    public void setSelect_to_data(String selectToData) {
        select_to_data = selectToData;
    }

    public String getOverride_sld() {
        return override_sld;
    }

    public void setOverride_SLD(String override_sld) {
        this.override_sld = override_sld;
    }

    public void setCols(List<String> fields) {
        String select = "Select ";
        for (int i = 0; i < fields.size(); i++) {
            String colmap = fields.get(i).replace("=", " As ");
            select = select + " " + colmap + ",";
            switch (i) {
            case 0:
                this.setCol1(fields.get(i));
                break;
            case 1:
                this.setCol2(fields.get(i));
                break;
            case 2:
                this.setCol3(fields.get(i));
                break;
            case 3:
                this.setCol4(fields.get(i));
                break;
            case 4:
                this.setCol5(fields.get(i));
                break;
            case 5:
                this.setCol6(fields.get(i));
                break;
            case 6:
                this.setCol7(fields.get(i));
                break;
            case 7:
                this.setCol8(fields.get(i));
                break;
            case 8:
                this.setCol9(fields.get(i));
                break;
            case 9:
                this.setCol10(fields.get(i));
                break;

            }

        }
        if (select.length() > 1) select = select.substring(0,select.length()-1);  // last , off
        select = select + " from analysis_data where analysis_id = "
                + Long.toString(this.getId());
        this.setSelect_to_data(select);
    }

    public String getColx(int i) {
        try
        {
            final Class c = this.getClass();
            Class parameters[] = {};
            Method m = c.getMethod("getCol"+String.valueOf(i),parameters);
            String valu = (String) m.invoke(this);
            return valu;
        }
        catch (Exception e) { }
        return null;

    }
}
