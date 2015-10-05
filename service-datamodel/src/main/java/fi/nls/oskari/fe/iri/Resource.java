package fi.nls.oskari.fe.iri;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.UUID;

public class Resource implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3560404821270949706L;

    public static Resource iri() {
        return new Resource(null, UUID.randomUUID());
    }

    public static Resource iri(QName qn) {
        return new Resource(qn);
    }

    public static Resource iri(String ns) {
        return new Resource(ns, UUID.randomUUID());
    }

    public static Resource iri(String ns, String localPart) {
        return new Resource(ns, localPart);
    }

    public static Resource iri(String ns, UUID uuid) {
        return new Resource(ns, uuid);
    }
    
   


    private final String uuid;

    private final String ns;

    private final String localPart;

    private final String _tostring;

    private Resource(QName qn) {
        ns = qn.getNamespaceURI();
        localPart = qn.getLocalPart();
        _tostring = __toString(null, null);
        uuid = null;
    }

    public Resource(String ns, String localPart) {
        this.ns = ns;
        this.localPart = localPart;
        _tostring = __toString(null, null);
        uuid = null;
    }

    public Resource(String ns, String localPart, UUID uuid) {
        this.ns = ns;
        this.localPart = localPart;
        this.uuid = uuid.toString();
        _tostring = __toString(null, null);
    }
    public Resource(String ns, String localPart, String uuid) {
        this.ns = ns;
        this.localPart = localPart;
        this.uuid = uuid;
        _tostring = __toString(null, null);
    }

    private Resource(String ns, UUID randomUUID) {
        uuid = randomUUID.toString();
        this.ns = ns;
        _tostring = __toString(null, null);
        localPart = null;
    }

    private String __toString(final String nsPrefix, String separator) {
        /* todo optimize */
        StringBuffer buf = new StringBuffer();
        if (nsPrefix != null) {
            buf.append(nsPrefix);
            buf.append(separator);
        } else if (ns != null) {
            buf.append(ns);
        }
        if (localPart != null) {
            buf.append(localPart);
        }
        if (uuid != null) {
            buf.append(uuid.toString());
        }

        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof Resource) {
            result = _tostring.equals(((Resource) other)._tostring);
        }
        return result;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getNs() {
        return ns;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return _tostring.hashCode();
    }

    public QName toQName() {
        return new QName(ns, localPart);
    }

    public String toString() {
        return _tostring;
    }

    public String toString(final String nsPrefix, String separator) {
        return __toString(nsPrefix, separator);
    }

    public Resource unique() {
        return new Resource(ns, localPart, UUID.randomUUID());
    }
    
    public Resource unique(String keyPart) {
        return new Resource(ns, localPart, keyPart);
    }

}
