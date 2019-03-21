package org.oskari.service.wfs3.model;

import java.util.List;

public class WFS3ReqClasses {

    private List<String> conformsTo;

    public List<String> getConformsTo() {
        return conformsTo;
    }

    public void setConformsTo(List<String> conformsTo) {
        this.conformsTo = conformsTo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WFS3ReqClasses other = (WFS3ReqClasses) obj;
        if (conformsTo == null) {
            if (other.conformsTo != null)
                return false;
        } else if (!conformsTo.equals(other.conformsTo))
            return false;
        return true;
    }

}
