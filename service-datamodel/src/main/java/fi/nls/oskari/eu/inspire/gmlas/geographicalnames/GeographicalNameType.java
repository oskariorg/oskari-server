package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import java.util.List;

import fi.nls.oskari.fe.xml.util.Nillable;

public class GeographicalNameType {

    public Nillable language;
    public Nillable nativeness;
    public Nillable nameStatus;
    public Nillable sourceOfName;

    public List<PronunciationOfName> pronunciation;
    public List<SpellingOfName> spelling;

    public Nillable grammaticalGender;
    public Nillable grammaticalNumber;
}
