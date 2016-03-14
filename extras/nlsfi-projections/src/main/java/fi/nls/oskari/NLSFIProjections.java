package fi.nls.oskari;

/**
 * Based on http://nassunet.nls.fi/jako/sovellusversio/mmlydin/tuotanto/201302/modules/projektio/doc/MML%20java-koordinaatistomuunnospaketti/liitteet/Java-koordinaatistomuunnokset.htm
 * The page is only available in the intranet of NLSFI
 * Date: 14.3.2016
 */
public enum NLSFIProjections {
    UNSUPPORTED("", -1, "Unsupported", true),

    NLSFI_jkj("NLSFI:jkj", 0, "Jakosovellusten tietokantakoordinaatisto", false),
    NLSFI_kkj("NLSFI:kkj", 1, "KKJ peruskoordinaatisto, kaistat 0-5", false),
    NLSFI_ykj("NLSFI:ykj", 2, "KKJ yhtenäiskoordinaatisto (YKJ = kaista 3)", false),
    NLSFI_euref("NLSFI:euref", 11, "ETRS-TM35FIN tasokoordinaatit", false),
    NLSFI_etrs_gk("NLSFI:etrs_gk", 7, "ETRS-GKn tasokoordinaatit", false),
    LATLON_kkj("LATLON:kkj", 4, "KKJ maantieteelliset koordinaatit", true),
    LATLON_etrs("LATLON:etrs", 5, "ETRS-TM35FIN maantieteelliset koordinaatit (~WGS84)", true),
    LONLAT_etrs("LONLAT:etrs", 6, "ETRS-TM35FIN maantieteelliset koordinaatit (~WGS84)", false),
    NLSFI_ETRS_TM34("NLSFI:ETRS-TM34",13, "ETRS-TM kaista 34 tasokoordinaatit", false),
    NLSFI_ETRS_TM36("NLSFI:ETRS-TM36",14, "ETRS-TM kaista 36 tasokoordinaatit", false),
    NLSFI_ETRS_TM("NLSFI:ETRS-TM",15, "ETRS-TM tasokoordinaatit", false),
    EPSG_3386("EPSG:3386", 3386, "EPSG KKJ kaista 0", true),
    EPSG_2391("EPSG:2391", 2391, "EPSG KKJ kaista 1", true),
    EPSG_2392("EPSG:2392", 2392, "EPSG KKJ kaista 2", true),
    EPSG_2393("EPSG:2393", 2393, "EPSG KKJ kaista 3", true),
    EPSG_2394("EPSG:2394", 2394, "EPSG KKJ kaista 4", true),
    EPSG_3387("EPSG:3387", 3387, "EPSG KKJ kaista 5", true),
    EPSG_3067("EPSG:3067", 3067, "EPSG ETRS-TM35FIN", false),
    EPSG_3046("EPSG:3046", 3046, "EPSG ETRS-TM34", true),
    EPSG_3047("EPSG:3047", 3047, "EPSG ETRS-TM35", true),
    EPSG_3048("EPSG:3048", 3048, "EPSG ETRS-TM36", true),
    EPSG_3126("EPSG:3126", 3126, "EPSG ETRS-GK19 (muuttuu -> 3873 v2013)", true),
    EPSG_3127("EPSG:3127", 3127, "EPSG ETRS-GK20 (muuttuu -> 3874 v2013)", true),
    EPSG_3128("EPSG:3128", 3128, "EPSG ETRS-GK21 (muuttuu -> 3875 v2013)", true),
    EPSG_3129("EPSG:3129", 3129, "EPSG ETRS-GK22 (muuttuu -> 3876 v2013)", true),
    EPSG_3130("EPSG:3130", 3130, "EPSG ETRS-GK23 (muuttuu -> 3877 v2013)", true),
    EPSG_3131("EPSG:3131", 3131, "EPSG ETRS-GK24 (muuttuu -> 3878 v2013)", true),
    EPSG_3132("EPSG:3132", 3132, "EPSG ETRS-GK25 (muuttuu -> 3879 v2013)", true),
    EPSG_3133("EPSG:3133", 3133, "EPSG ETRS-GK26 (muuttuu -> 3880 v2013)", true),
    EPSG_3134("EPSG:3134", 3134, "EPSG ETRS-GK27 (muuttuu -> 3881 v2013)", true),
    EPSG_3135("EPSG:3135", 3135, "EPSG ETRS-GK28 (muuttuu -> 3882 v2013)", true),
    EPSG_3136("EPSG:3136", 3136, "EPSG ETRS-GK29 (muuttuu -> 3883 v2013)", true),
    EPSG_3137("EPSG:3137", 3137, "EPSG ETRS-GK30 (muuttuu -> 3884 v2013)", true),
    EPSG_3138("EPSG:3138", 3138, "EPSG ETRS-GK31 (muuttuu -> 3885 v2013)", true),
    EPSG_3873("EPSG:3873", 3873, "EPSG ETRS-GK19", true),
    EPSG_3874("EPSG:3874", 3874, "EPSG ETRS-GK20", true),
    EPSG_3875("EPSG:3875", 3875, "EPSG ETRS-GK21", true),
    EPSG_3876("EPSG:3876", 3876, "EPSG ETRS-GK22", true),
    EPSG_3877("EPSG:3877", 3877, "EPSG ETRS-GK23", true),
    EPSG_3878("EPSG:3878", 3878, "EPSG ETRS-GK24", true),
    EPSG_3879("EPSG:3879", 3879, "EPSG ETRS-GK25", true),
    EPSG_3880("EPSG:3880", 3880, "EPSG ETRS-GK26", true),
    EPSG_3881("EPSG:3881", 3881, "EPSG ETRS-GK27", true),
    EPSG_3882("EPSG:3882", 3882, "EPSG ETRS-GK28", true),
    EPSG_3883("EPSG:3883", 3883, "EPSG ETRS-GK29", true),
    EPSG_3884("EPSG:3884", 3884, "EPSG ETRS-GK30", true),
    EPSG_3885("EPSG:3885", 3885, "EPSG ETRS-GK31", true),
    EPSG_4258("EPSG:4258", 4258, "EPSG ETRS-TM35FIN maantieteelliset koordinaatit (~WGS84)", true),
    EPSG_5048("EPSG:5048", 5048, "EPSG ETRS-TM35FIN", true);


    String code;
    int num;
    String desc;
    boolean northFirst;

    NLSFIProjections(String code, int num, String desc, boolean axisNorthFirst) {
        this.code = code;
        this.num = num;
        this.desc = desc;
        this.northFirst = axisNorthFirst;
    }

    public static NLSFIProjections forCode(String code) {
        for(NLSFIProjections p : values()) {
            if(p.code.equals(code)) {
                return p;
            }
        }
        return UNSUPPORTED;
    }
}
