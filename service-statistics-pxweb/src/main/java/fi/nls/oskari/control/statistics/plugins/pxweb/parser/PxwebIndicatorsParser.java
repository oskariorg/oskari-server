package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.control.statistics.plugins.pxweb.PxwebConfig;
import fi.nls.oskari.control.statistics.plugins.pxweb.json.PxwebItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PxwebIndicatorsParser {
    private final static Logger LOG = LogFactory.getLogger(PxwebIndicatorsParser.class);

    private PxwebConfig config;
    private ObjectMapper mapper = new ObjectMapper();

    public PxwebIndicatorsParser(PxwebConfig config) {
        this.config = config;
    }

    public List<StatisticalIndicator> parse(List<DatasourceLayer> layers) {
        return parse(null, null, layers);
    }

    public List<StatisticalIndicator> parse(PxwebItem parent, String path, List<DatasourceLayer> layers) {
        List<StatisticalIndicator> indicators = new ArrayList<>();
        try {
            final String url = getUrl(path);
            String jsonResponse = IOHelper.getURL(url);
            List<PxwebItem> list =
                    mapper.readValue(jsonResponse, mapper.getTypeFactory().constructCollectionType(List.class, PxwebItem.class));
            for(PxwebItem item : list) {
                if("l".equalsIgnoreCase(item.type)) {
                    // recurse to pxweb "folder"
                    indicators.addAll(parse(item, getPath(path, item.id), layers));
                    continue;
                }
                if(!"t".equalsIgnoreCase(item.type)) {
                    // only recognize l and t types
                    continue;
                }
                StatisticalIndicator ind = new StatisticalIndicator();
                ind.setId(item.id);
                ind.addName(PropertyUtil.getDefaultLanguage(), item.text);
                setupMetadata(ind, path);
                for(DatasourceLayer layer : layers) {
                    StatisticalIndicatorLayer l = new StatisticalIndicatorLayer(layer.getMaplayerId(), ind.getId());
                    l.addParam("baseUrl", url);
                    ind.addLayer(l);
                }
                indicators.add(ind);
            }

        } catch (IOException e) {
            LOG.error(e, "Error getting indicators from Pxweb datasource:", config.getUrl());
        }
        return indicators;
    }

    private String getUrl(String path) {
        if(path == null) {
            // Example: "http://pxweb.hel.ninja/PXWeb/api/v1/en/hri/hri/"
            return config.getUrl();
        }
        String url = config.getUrl() + "/" + IOHelper.urlEncode(path) + "/";
        return IOHelper.fixPath(url);
    }


    private String getPath(String path, String nextPart) {
        if(path == null) {
            return nextPart;
        }
        String url = path + "/" +  IOHelper.urlEncode(nextPart);
        return url.replaceAll("//", "/");
    }
/*
{
	"title": "Helsingin asuntotuotanto talotyypin, hallintaperusteen, rahoitusmuodon ja huoneistotyypin mukaan",
	"variables": [{
		"code": "Alue",
		"text": "Alue",
		"values": ["0910000000", "0911000000", "0911101000", "0911101010", "0911101020", "0911101080", "0911102000", "0911102030", "0911102050", "0911102060", "0911102070", "0911102090", "0911102204", "0911102520", "0911102531", "0911103000", "0911103040", "0911103130", "0911103201", "0911103202", "0911103203", "0911104000", "0911104140", "0911105000", "0911105310", "0911105311", "0911105312", "0911105313", "0911105314", "0912000000", "0912201000", "0912201150", "0912201160", "0912201161", "0912201162", "0912201180", "0912202000", "0912202301", "0912202302", "0912202303", "0912202304", "0912202305", "0912202306", "0912203000", "0912203291", "0912203292", "0912203293", "0912203294", "0912204000", "0912204320", "0912204461", "0912204462", "0912204463", "0912204464", "0912204465", "0912205000", "0912205331", "0912205332", "0912205333", "0912205334", "0912205335", "0912205336", "0913000000", "0913301000", "0913301100", "0913301101", "0913301102", "0913301103", "0913301104", "0913301111", "0913301112", "0913301113", "0913302000", "0913302121", "0913302122", "0913303000", "0913303210", "0913303211", "0913303212", "0913303213", "0913303220", "0913304000", "0913304171", "0913304172", "0913304173", "0913304174", "0913305000", "0913305231", "0913305232", "0913305240", "0913305250", "0913305260", "0913305270", "0914000000", "0914401000", "0914401281", "0914401282", "0914401283", "0914401286", "0914402000", "0914402341", "0914403000", "0914403351", "0914403352", "0914403354", "0914404000", "0914404284", "0914404285", "0914404287", "0914405000", "0914405342", "0914405353", "0915000000", "0915501000", "0915501361", "0915501362", "0915501363", "0915501364", "0915501383", "0915501386", "0915502000", "0915502370", "0915503000", "0915503381", "0915503382", "0915503384", "0915503385", "0915503391", "0915503392", "0915504000", "0915504401", "0915504403", "0915505000", "0915505402", "0915505411", "0915505412", "0915505413", "0915505415", "0915506000", "0915506414", "0916000000", "0916601000", "0916601190", "0916601420", "0916602000", "0916602431", "0916602432", "0916602433", "0916602434", "0916602440", "0916603000", "0916603480", "0916603491", "0916603492", "0916603493", "0916603494", "0916603495", "0916603500", "0916603510", "0916603532", "0917000000", "0917701000", "0917701451", "0917701452", "0917701453", "0917701455", "0917701456", "0917701457", "0917702000", "0917702454", "0917703000", "0917703471", "0917703472", "0917703473", "0917703474", "0917703475", "0917704000", "0917704541", "0917704542", "0917704543", "0917704544", "0917704545", "0917704546", "0917704547", "0917704548", "0917704549", "0918000000", "0918801000", "0918801550", "0918801560", "0918801570", "0918801580", "0918801591", "0918801592", "0919999999", "0919980000", "0919970000"],
		"valueTexts": ["091 Helsinki", "091 1 Eteläinen suurpiiri", "091 101 Vironniemen peruspiiri", "091 10 Kruununhaka", "091 20 Kluuvi", "091 80 Katajanokka", "091 102 Ullanlinnan peruspiiri", "091 30 Kaartinkaupunki", "091 50 Punavuori", "091 60 Eira", "091 70 Ullanlinna", "091 90 Kaivopuisto", "091 204 Hernesaari", "091 520 Suomenlinna", "091 531 Länsisaaret", "091 103 Kampinmalmin peruspiiri", "091 40 Kamppi", "091 130 Etu-Töölö", "091 201 Ruoholahti", "091 202 Lapinlahti", "091 203 Jätkäsaari", "091 104 Taka-Töölön peruspiiri", "091 140 Taka-Töölö", "091 105 Lauttasaaren peruspiiri", "091 310 Lauttasaari (Ent.)", "091 311 Kotkavuori", "091 312 Vattuniemi", "091 313 Myllykallio", "091 314 Koivusaari", "091 2 Läntinen suurpiiri", "091 201 Reijolan peruspiiri", "091 150 Meilahti", "091 160 Ruskeasuo (Ent.)", "091 161 Vanha Ruskeasuo", "091 162 Pikku Huopalahti", "091 180 Laakso", "091 202 Munkkiniemen peruspiiri", "091 301 Vanha Munkkiniemi", "091 302 Kuusisaari", "091 303 Lehtisaari", "091 304 Munkkivuori", "091 305 Niemenmäki", "091 306 Talinranta", "091 203 Haagan peruspiiri", "091 291 Etelä-Haaga", "091 292 Kivihaka", "091 293 Pohjois-Haaga", "091 294 Lassila", "091 204 Pitäjänmäen peruspiiri", "091 320 Konala", "091 461 Pajamäki", "091 462 Tali", "091 463 Reimarla", "091 464 Marttila", "091 465 Pitäjänmäen yritysalue", "091 205 Kaarelan peruspiiri", "091 331 Kannelmäki", "091 332 Maununneva", "091 333 Malminkartano", "091 334 Hakuninmaa", "091 335 Kuninkaantammi", "091 336 Honkasuo", "091 3 Keskinen suurpiiri", "091 301 Kallion peruspiiri", "091 100 Sörnäinen (Ent.)", "091 101 Vilhonvuori", "091 102 Kalasatama", "091 103 Sompasaari", "091 104 Hanasaari", "091 111 Siltasaari", "091 112 Linjat", "091 113 Torkkelinmäki", "091 302 Alppiharjun peruspiiri", "091 121 Harju", "091 122 Alppila", "091 303 Vallilan peruspiiri", "091 210 Hermanni (Ent.)", "091 211 Hermanninmäki", "091 212 Hermanninranta", "091 213 Kyläsaari", "091 220 Vallila", "091 304 Pasilan peruspiiri", "091 171 Länsi-Pasila", "091 172 Pohjois-Pasila", "091 173 Itä-Pasila", "091 174 Keski-Pasila", "091 305 Vanhankaupungin peruspiiri", "091 231 Toukola", "091 232 Arabianranta", "091 240 Kumpula", "091 250 Käpylä", "091 260 Koskela", "091 270 Vanhakaupunki", "091 4 Pohjoinen suurpiiri", "091 401 Maunulan peruspiiri", "091 281 Pirkkola", "091 282 Maunula", "091 283 Metsälä", "091 286 Maunulanpuisto", "091 402 Länsi-Pakilan peruspiiri", "091 341 Länsi-Pakila", "091 403 Tuomarinkylän peruspiiri", "091 351 Paloheinä", "091 352 Torpparinmäki", "091 354 Haltiala", "091 404 Oulunkylän peruspiiri", "091 284 Patola", "091 285 Veräjämäki", "091 287 Veräjälaakso", "091 405 Itä-Pakilan peruspiiri", "091 342 Itä-Pakila", "091 353 Tuomarinkartano", "091 5 Koillinen suurpiiri", "091 501 Latokartanon peruspiiri", "091 361 Viikinranta", "091 362 Latokartano", "091 363 Viikin tiedepuisto", "091 364 Viikinmäki", "091 383 Pihlajamäki", "091 386 Pihlajisto", "091 502 Pukinmäen peruspiiri", "091 370 Pukinmäki", "091 503 Malmin peruspiiri", "091 381 Ylä-Malmi", "091 382 Ala-Malmi", "091 384 Tattariharju", "091 385 Malmin lentokenttä", "091 391 Tapaninvainio", "091 392 Tapanila", "091 504 Suutarilan peruspiiri", "091 401 Siltamäki", "091 403 Töyrynummi", "091 505 Puistolan peruspiiri", "091 402 Tapulikaupunki", "091 411 Puistola", "091 412 Heikinlaakso", "091 413 Tattarisuo", "091 415 Alppikylä", "091 506 Jakomäen peruspiiri", "091 414 Jakomäki", "091 6 Kaakkoinen suurpiiri", "091 601 Kulosaaren peruspiiri", "091 190 Mustikkamaa-Korkeasaari", "091 420 Kulosaari", "091 602 Herttoniemen peruspiiri", "091 431 Länsi-Herttoniemi", "091 432 Roihuvuori", "091 433 Herttoniemen yritysalue", "091 434 Herttoniemenranta", "091 440 Tammisalo", "091 603 Laajasalon peruspiiri", "091 480 Vartiosaari", "091 491 Yliskylä", "091 492 Jollas", "091 493 Tullisaari", "091 494 Kruunuvuorenranta", "091 495 Hevossalmi", "091 500 Villinki", "091 510 Santahamina", "091 532 Itäsaaret", "091 7 Itäinen suurpiiri", "091 701 Vartiokylän peruspiiri", "091 451 Vartioharju", "091 452 Puotila", "091 453 Puotinharju", "091 455 Marjaniemi", "091 456 Roihupelto", "091 457 Itäkeskus", "091 702 Myllypuron peruspiiri", "091 454 Myllypuro", "091 703 Mellunkylän peruspiiri", "091 471 Kontula", "091 472 Vesala", "091 473 Mellunmäki", "091 474 Kivikko", "091 475 Kurkimäki", "091 704 Vuosaaren peruspiiri", "091 541 Keski-Vuosaari", "091 542 Nordsjön kartano", "091 543 Uutela", "091 544 Meri-Rastila", "091 545 Kallahti", "091 546 Aurinkolahti", "091 547 Rastila", "091 548 Niinisaari", "091 549 Mustavuori", "091 8 Östersundomin suurpiiri", "091 801 Östersundomin peruspiiri", "091 550 Östersundom", "091 560 Salmenkallio", "091 570 Talosaari", "091 580 Karhusaari", "091 591 Landbo", "091 592 Puroniitty", "091 999 Muut", "091 998 Kantakaupunki", "091 997 Esikaupungit"]
	}, {
		"code": "Talotyyppi",
		"text": "Talotyyppi",
		"values": ["all", "1", "2", "3", "4", "5"],
		"valueTexts": ["Yhteensä", "Yhden asunnon talot", "Muut erilliset pientalot", "Rivi- tai ketjutalot", "Asuinkerrostalot", "Muut rakennukset"]
	}, {
		"code": "Hallintaperuste",
		"text": "Hallintaperuste",
		"values": ["all", "1", "2", "3", "4"],
		"valueTexts": ["Asunnot yhteensä", "Omistusasunnot", "Vuokra-asunnot", "Asumisoikeusasunnot", "Muut asunnot"]
	}, {
		"code": "Rahoitusmuoto",
		"text": "Rahoitusmuoto",
		"values": ["all", "3", "1"],
		"valueTexts": ["Yhteensä", "Vapaarahoitteinen*", "Arava/pitkä korkotuki**"]
	}, {
		"code": "Huoneistotyyppi",
		"text": "Huoneistotyyppi",
		"values": ["all", "1", "2", "3", "4", "5", "6", "7", "8"],
		"valueTexts": ["Yhteensä", "1 h + kk/kt", "1 h + k/tk", "2 h + kk/kt", "2 h + k/tk", "3 h + kk/kt/k/tk", "4 h + kk/kt/k/tk", "5 h + kk/kt/k/tk", "6 h + kk/kt/k/tk -"]
	}, {
		"code": "Yksikkö",
		"text": "Yksikkö",
		"values": ["1", "2"],
		"valueTexts": ["Asuntojen lukumäärä", "Asuntojen pinta-ala m2"]
	}, {
		"code": "Vuosi",
		"text": "Vuosi",
		"values": ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"],
		"valueTexts": ["2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015"]
	}]
}
 */
    private void setupMetadata(StatisticalIndicator indicator, String path) {
        final StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        indicator.setDataModel(selectors);
        final JSONObject json = getMetadata(indicator, path);
        if(json == null) {
            // TODO: throw an error maybe? same with unexpected response
            return;
        }

        try {
            JSONArray variables = json.optJSONArray("variables");
            if (variables == null) {
                // TODO: throw an error maybe? same with connection error
                return;
            }
            for (int i = 0; i < variables.length(); i++) {
                JSONObject var = variables.optJSONObject(i);
                final String id = var.optString("code");
                if (config.getIgnoredVariables().contains(id)) {
                    continue;
                }
                StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(id);
                selector.setName(var.optString("text"));

                JSONArray values = var.optJSONArray("values");
                JSONArray valueTexts = var.optJSONArray("valueTexts");
                for (int j = 0; j < values.length(); j++) {
                    selector.addAllowedValue(values.optString(j), valueTexts.optString(j));
                }
                selectors.addDimension(selector);
            }
        } catch (Exception ex) {
            LOG.error(ex, "Error parsing indicator metadata from Pxweb datasource:", json);
        }
    }

    private JSONObject getMetadata(StatisticalIndicator indicator, String path) {
        final String url = getUrl(path) + indicator.getId();
        try {
            return JSONHelper.createJSONObject(IOHelper.getURL(url));
        } catch (IOException ex) {
            LOG.error(ex, "Error getting indicator metadata from Pxweb datasource:", url);
        }
        return null;

    }

}
