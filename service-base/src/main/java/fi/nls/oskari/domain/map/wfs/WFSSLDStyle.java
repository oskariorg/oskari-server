package fi.nls.oskari.domain.map.wfs;

/**
 * Created with IntelliJ IDEA.
 * User: EVALANTO
 * Date: 5.9.2013
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class WFSSLDStyle {

    private int id;
    private String name;
  private String sldStyle;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSldStyle() {
        return sldStyle;
    }

    public void setSldStyle(String sldStyle) {
        this.sldStyle = sldStyle;
    }

}
