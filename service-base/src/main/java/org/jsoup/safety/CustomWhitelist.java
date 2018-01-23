package org.jsoup.safety;

import fi.nls.oskari.util.PropertyUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.util.List;

public class CustomWhitelist extends Whitelist {

    private boolean allowDataUrlsForImages = false;

    public CustomWhitelist() {
        this(null);
    }

    public CustomWhitelist(String functionality) {
        super();
        copiedFromRelaxed();
        // setup config
        init(functionality);
    }

    /**
     * These are copied from Whitelist.relaxed().
     */
    private void copiedFromRelaxed() {
        addTags("a", "b", "blockquote", "br", "caption", "cite", "code", "col", "colgroup", "dd", "div", "dl", "dt",
                "em", "h1", "h2", "h3", "h4", "h5", "h6", "i", "img", "li", "ol", "p", "pre", "q", "small", "span",
                "strike", "strong", "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul");
        addAttributes("a", "href", "title");
        addAttributes("blockquote", "cite");
        addAttributes("col", "span", "width");
        addAttributes("colgroup", "span", "width");
        addAttributes("img", "align", "alt", "height", "src", "title", "width");
        addAttributes("ol", "start", "type");
        addAttributes("q", "cite");
        addAttributes("table", "summary", "width");
        addAttributes("td", "abbr", "axis", "colspan", "rowspan", "width");
        addAttributes("th", "abbr", "axis", "colspan", "rowspan", "scope", "width");
        addAttributes("ul", "type");
        addProtocols("a", "href", "ftp", "http", "https", "mailto");
        addProtocols("blockquote", "cite", "http", "https");
        addProtocols("cite", "cite", "http", "https");
        addProtocols("img", "src", "http", "https");
        addProtocols("q", "cite", "http", "https");
    }

    /**
     * Initializes custom configuration based on properties (added to relaxed settings):
     * # allowed tags
     * html.whitelist=tags,as,comma,separated,list
     * # allowed attributes for a tag
     * html.whitelist.attr.[tag]=attributes,for,tag,as,comma,separated,list
     * # allowed protocols in an attribute value for a tag
     * html.whitelist.attr.[tag].protocol.[attr]=ftp
     * # true if <img src="data:..." /> data urls in images are allowed
     * html.whitelist.attr.img.dataurl=[boolean]
     *
     * For functionality specific config prefix the properties like "gfi.html.whitelist=tags,allowed,in,gfi,responses"
     * @param functionality
     */
    protected void init(String functionality) {
        String prefix = "";
        if (functionality != null) {
            prefix = functionality + ".";
        }

        // Extra-tags that are allowed
        final String[] tags = PropertyUtil.getCommaSeparatedList(prefix + "html.whitelist");
        addTags(tags);

        // Global attributes that are allowed
        String[] allAttributes = PropertyUtil.getCommaSeparatedList(prefix + "html.whitelist.attr");
        if (allAttributes.length > 0) {
            // these are available for all tags (":all" is a jsoup pseudotag that means "any tag")
            addAttributes(":all", allAttributes);
        }
        // Tag specific attributes/protocols in attr values that are allowed
        String propPrefix = prefix + "html.whitelist.attr.";
        List<String> attrProps = PropertyUtil.getPropertyNamesStartingWith(propPrefix);
        for (String attrProp : attrProps) {
            // attrProp is like "html.whitelist.attr.a" or "html.whitelist.attr.img.protocol.src"
            // key is like "a" or "img.protocol.src"
            final String key = attrProp.substring(propPrefix.length());
            String[] parts = key.split("\\.");
            if (parts.length == 1) {
                // html.whitelist.attr.a=href,target -> for <a> attributes href and target are allowed
                addAttributes(parts[0], PropertyUtil.getCommaSeparatedList(attrProp));
            } else if (parts.length == 3 && "protocol".equals(parts[1])) {
                // html.whitelist.attr.img.protocol.src=ftp -> for <img src="ftp://something"> ftp protocol is allowed
                addProtocols(parts[0], parts[1], PropertyUtil.getCommaSeparatedList(attrProp));
            }
        }
        // check for dataurls
        allowDataUrlsForImages(PropertyUtil.getOptional(prefix + "html.whitelist.attr.img.dataurl", false));
    }

    private void allowDataUrlsForImages(boolean enabled) {
        allowDataUrlsForImages = enabled;
        // required for data-urls to function properly
        preserveRelativeLinks(allowDataUrlsForImages);
    }

    /**
     * The real reason for this class... allow data urls in images
     * @param tagName
     * @param el
     * @param attr
     * @return
     */
    @Override
    protected boolean isSafeAttribute(String tagName, Element el, Attribute attr) {
        return (allowDataUrlsForImages && "img".equals(tagName)
                    && "src".equals(attr.getKey())
                    && attr.getValue().startsWith("data:")) ||
                super.isSafeAttribute(tagName, el, attr);
    }
}
