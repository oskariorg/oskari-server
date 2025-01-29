package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CustomSafelistTest {

    private static final String IMG_WITH_DATA_URL = "<img src=\"data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA\n" +
            "    AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO\n" +
            "        9TXL0Y4OHwAAAABJRU5ErkJggg==\" alt=\"Red dot\">";
    @Test
    public void isSafeAttributeDataURLDisabled() {
        CustomSafelist list = new CustomSafelist();
        String result = Jsoup.clean(IMG_WITH_DATA_URL, list);
        Assertions.assertEquals("<img alt=\"Red dot\">", result);
    }

    @Test
    public void isSafeAttributeDataURLEnabled() {
        CustomSafelist list = new CustomSafelist();
        list.allowDataUrlsForImages(true);
        String result = Jsoup.clean(IMG_WITH_DATA_URL, list);
        Assertions.assertEquals(IMG_WITH_DATA_URL, result);
    }
}