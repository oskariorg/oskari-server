package fi.nls.oskari.domain.map.analysis;

import org.locationtech.jts.geom.Geometry;

import java.time.OffsetDateTime;

public class AnalysisData {
    private long id;
    private long analysisId;

    private String uuid;

    private String t1;
    private String t2;
    private String t3;
    private String t4;
    private String t5;
    private String t6;
    private String t7;
    private String t8;

    private double n1;
    private double n2;
    private double n3;
    private double n4;
    private double n5;
    private double n6;
    private double n7;
    private double n8;

    private OffsetDateTime d1;
    private OffsetDateTime d2;
    private OffsetDateTime d3;
    private OffsetDateTime d4;

    private String wkt;

    private Geometry geometry;

    private int databaseSRID;

    private OffsetDateTime created;

    private OffsetDateTime updated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(long analysisId) {
        this.analysisId = analysisId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getT1() {
        return t1;
    }

    public void setT1(String t1) {
        this.t1 = t1;
    }

    public String getT2() {
        return t2;
    }

    public void setT2(String t2) {
        this.t2 = t2;
    }

    public String getT3() {
        return t3;
    }

    public void setT3(String t3) {
        this.t3 = t3;
    }

    public String getT4() {
        return t4;
    }

    public void setT4(String t4) {
        this.t4 = t4;
    }

    public String getT5() {
        return t5;
    }

    public void setT5(String t5) {
        this.t5 = t5;
    }

    public String getT6() {
        return t6;
    }

    public void setT6(String t6) {
        this.t6 = t6;
    }

    public String getT7() {
        return t7;
    }

    public void setT7(String t7) {
        this.t7 = t7;
    }

    public String getT8() {
        return t8;
    }

    public void setT8(String t8) {
        this.t8 = t8;
    }

    public double getN1() {
        return n1;
    }

    public void setN1(double n1) {
        this.n1 = n1;
    }

    public double getN2() {
        return n2;
    }

    public void setN2(double n2) {
        this.n2 = n2;
    }

    public double getN3() {
        return n3;
    }

    public void setN3(double n3) {
        this.n3 = n3;
    }

    public double getN4() {
        return n4;
    }

    public void setN4(double n4) {
        this.n4 = n4;
    }

    public double getN5() {
        return n5;
    }

    public void setN5(double n5) {
        this.n5 = n5;
    }

    public double getN6() {
        return n6;
    }

    public void setN6(double n6) {
        this.n6 = n6;
    }

    public double getN7() {
        return n7;
    }

    public void setN7(double n7) {
        this.n7 = n7;
    }

    public double getN8() {
        return n8;
    }

    public void setN8(double n8) {
        this.n8 = n8;
    }

    public OffsetDateTime getD1() {
        return d1;
    }

    public void setD1(OffsetDateTime d1) {
        this.d1 = d1;
    }

    public OffsetDateTime getD2() {
        return d2;
    }

    public void setD2(OffsetDateTime d2) {
        this.d2 = d2;
    }

    public OffsetDateTime getD3() {
        return d3;
    }

    public void setD3(OffsetDateTime d3) {
        this.d3 = d3;
    }

    public OffsetDateTime getD4() {
        return d4;
    }

    public void setD4(OffsetDateTime d4) {
        this.d4 = d4;
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public int getDatabaseSRID() {
        return databaseSRID;
    }

    public void setDatabaseSRID(int databaseSRID) {
        this.databaseSRID = databaseSRID;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }
}
