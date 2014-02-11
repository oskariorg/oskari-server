package fi.nls.oskari.search.ktjkiiwfs;

public class RegisterUnitParcelSearchResult {

    String gmlID ;
    String registerUnitID;
    String registerUnitGmlID ;
    String E ;
    String N ;
    String BBOX;
    
    public String getGmlID() {
        return gmlID;
    }
    public void setGmlID(String gmlID )  {
        this.gmlID = gmlID;
    }
    
    public String getRegisterUnitID() {
        return registerUnitID;
    }
    public void setRegisterUnitID(String registerUnitID) {
        this.registerUnitID = registerUnitID;
    }
    
    public String getRegisterUnitGmlID() {
        return registerUnitGmlID;
    }
    public void setRegisterUnitGmlID(String registerUnitGmlID) {
        this.registerUnitGmlID = registerUnitGmlID;
    }
    public String getE() {
        return E;
    }
    public void setE(String e) {
        E = e;
    }
    public String getN() {
        return N;
    }
    public void setN(String n) {
        N = n;
    }
    
    public String getLon() {
        return E;
    }
    public String getLat() {
        return N;
    }

    public String getBBOX() {
        return BBOX;
    }

    public void setBBOX(String BBOX) {
        this.BBOX = BBOX;
    }
}
