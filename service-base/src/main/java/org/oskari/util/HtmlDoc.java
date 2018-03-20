package org.oskari.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.CustomWhitelist;
import org.jsoup.select.Elements;

import java.util.Iterator;

public class HtmlDoc {

    private Document doc;

    public HtmlDoc(String html) {
        if (html != null) {
            doc = Jsoup.parse(html);
        }
    }

    public String getFiltered() {
        return getFiltered(null);
    }

    public String getFiltered(String functionality) {
        if (doc == null) {
            return null;
        }
        Cleaner cleaner = new Cleaner(new CustomWhitelist(functionality));
        Document safeDoc = cleaner.clean(doc);
        Element body = safeDoc.body();
        if (body == null) {
            return safeDoc.toString();
        }
        return body.html();
    }

    public HtmlDoc modifyLinks(String baseUrl) {
        if (doc == null) {
            return this;
        }
        doc.setBaseUri(baseUrl);
        Elements links = doc.select("a[href]"); // a with href
        Iterator<Element> linkRefs = links.iterator();
        while (linkRefs.hasNext()) {
            Element link = linkRefs.next();
            if (!link.hasAttr("target")) {
                link.attr("target", "_blank");
            }
            link.attr("href", link.absUrl("href"));
        }

        Elements images = doc.select("img[src]");
        Iterator<Element> imageRefs = images.iterator();
        while (imageRefs.hasNext()) {
            Element img = imageRefs.next();
            if (!img.attr("src").startsWith("data:")) {
                img.attr("src", img.absUrl("src"));
            }
        }
        return this;
    }
}
