package fi.nls.oskari.control.metadata;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EmptyNodeList implements NodeList {

    public Node item(int index) {
        throw new ArrayIndexOutOfBoundsException("Trying to get element at " + index + " from empty list!");
    }

    public int getLength() {
        return 0;
    }
}
