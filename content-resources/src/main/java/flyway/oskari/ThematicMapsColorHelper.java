package flyway.oskari;

public class ThematicMapsColorHelper {

    private static final String[] COLORSETS_DIV = {
            "BrBG",
            "PiYG",
            "PRGn",
            "PuOr",
            "RdBu",
            "RdGy",
            "RdYlBu",
            "RdYlGn",
            "Spectral"
    };

    private static final String[] COLORSETS_SEQ = {
            "Blues",
            "BuGn",
            "BuPu",
            "GnBu",
            "Greens",
            "Greys",
            "Oranges",
            "OrRd",
            "PuBu",
            "PuBuGn",
            "PuRd",
            "Purples",
            "RdPu",
            "Reds",
            "YlGn",
            "YlGnBu",
            "YlOrBr",
            "YlOrRd"
    };

    private static final String[] COLORSETS_QUAL = {
            "Accent",
            "Dark2",
            "Paired",
            "Pastel1",
            "Pastel2",
            "Set1",
            "Set2",
            "Set3"
    };

    public static String getColorNameFromIndex(String set, int colorIndex) {
        switch (set) {
            case "div": return COLORSETS_DIV[colorIndex];
            case "seq": return COLORSETS_SEQ[colorIndex];
            case "qual": return COLORSETS_QUAL[colorIndex];
            default: return null;
        }
    }
}
