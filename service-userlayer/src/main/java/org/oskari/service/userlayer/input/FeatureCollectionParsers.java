package org.oskari.service.userlayer.input;

public class FeatureCollectionParsers {

    public static boolean isMainFile(String fileExt) {
        fileExt = fileExt.toUpperCase();
        switch (fileExt) {
        case GPXParser.SUFFIX:
        case KMLParser.SUFFIX:
        case MIFParser.SUFFIX:
        case SHPParser.SUFFIX:
            return true;
        default:
            return false;
        }
    }

    public static FeatureCollectionParser byFileExt(String fileExt) {
        fileExt = fileExt.toUpperCase();
        switch (fileExt) {
        case GPXParser.SUFFIX: return new GPXParser();
        case KMLParser.SUFFIX: return new KMLParser();
        case MIFParser.SUFFIX: return new MIFParser();
        case SHPParser.SUFFIX: return new SHPParser();
        default: return null;
        }
    }

}
