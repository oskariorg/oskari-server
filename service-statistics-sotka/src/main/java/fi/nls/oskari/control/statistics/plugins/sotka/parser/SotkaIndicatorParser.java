package fi.nls.oskari.control.statistics.plugins.sotka.parser;

import java.util.*;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.sotka.SotkaConfig;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorMetadata;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SotkaIndicatorParser {
    private final static Logger LOG = LogFactory.getLogger(SotkaIndicatorParser.class);
    private SotkaConfig config;

    public SotkaIndicatorParser(SotkaConfig obj) {
        config = obj;
    }

    public StatisticalIndicator parse(JSONObject json, Map<String, Long> sotkaLayersToOskariLayers) {
        try {
            StatisticalIndicator indicator = createIndicator(json, sotkaLayersToOskariLayers);
            if(indicator == null) {
                return null;
            }
            setupMetadata(indicator);
            return indicator;
        } catch (Exception e) {
            LOG.error("Error in mapping Sotka Indicators response to Oskari model: " + e.getMessage(), e);
        }
        return null;
    }

    /*
    Parses the basic info for indicator. For full info setupMetadata() call is required. Input JSON is like this:
    {
	"id": 4,
	"title": {
		"fi": "Mielenterveyden häiriöihin sairaalahoitoa saaneet 0 - 17-vuotiaat / 1 000 vastaavanikäistä",
		"en": "Hospital care for mental disorders, recipients aged 0-17 per 1000 persons of the same age",
		"sv": "0 - 17-åringar som vårdats på sjukhus för psykiska störningar / 1 000 i samma åldrar"
	},
	"organization": {
		"id": 2,
		"title": {
			"fi": "Terveyden ja hyvinvoinnin laitos (THL)",
			"en": "Institute for Health and Welfare (THL)",
			"sv": "Institutet för hälsa och välfärd (THL)"
		}
	},
	"classifications": {
		"sex": {
			"values": ["male", "female", "total"]
		},
		"region": {
			"values": ["Kunta", "Maakunta", "Erva", "Aluehallintovirasto", "Sairaanhoitopiiri", "Maa", "Suuralue", "Seutukunta", "Nuts1"]
		}
	}
}
     */
    StatisticalIndicator createIndicator(JSONObject json, Map<String, Long> sotkaLayersToOskariLayers) {
        StatisticalIndicator ind = new StatisticalIndicator();
        try {
            String indicatorId = String.valueOf(json.getInt("id"));
            ind.setId(indicatorId);

            // parse layers first since if none match -> we don't need to parse the rest
            if (!json.getJSONObject("classifications").has("region")) {
                LOG.error("Region missing from indicator: " + indicatorId + ": " + String.valueOf(ind.getName()));
                return null;
            }
            JSONArray sotkaRegionsets = json.getJSONObject("classifications").getJSONObject("region").getJSONArray("values");
            for (int i = 0; i < sotkaRegionsets.length(); i++) {
                String sotkaLayerName = sotkaRegionsets.getString(i);
                Long oskariLayerId = sotkaLayersToOskariLayers.get(sotkaLayerName.toLowerCase());
                if (oskariLayerId != null) {
                    ind.addLayer(new StatisticalIndicatorLayer(oskariLayerId, indicatorId));
                }
            }
            if(ind.getLayers().isEmpty()) {
                return null;
            }

            // layers ok - proceed with the rest of the data
            JSONObject nameJson = json.getJSONObject("title");
            Iterator<String> names = nameJson.keys();
            while (names.hasNext()) {
                String key = names.next();
                ind.addName(key, nameJson.getString(key));
            }
            ind.setSource(toLocalizationMap(json.getJSONObject("organization").getJSONObject("title")));

            // Note that the following will just skip the "region" part already projected into layers.
            ind.setDataModel(createModel(json.getJSONObject("classifications")));
        } catch (JSONException e) {
            e.printStackTrace();
            LOG.error("Could not read data from Sotka Indicator JSON.", e);
            return null;
        }
        return ind;
    }
    private StatisticalIndicatorDataModel createModel(JSONObject jsonObject) throws JSONException {
        // Note that the key "region" must be skipped, because it was already serialized as layers.
        StatisticalIndicatorDataModel selectors = new StatisticalIndicatorDataModel();
        @SuppressWarnings("unchecked")
        Iterator<String> names = jsonObject.keys();
        while (names.hasNext()) {
            String key = names.next();
            if (key.equals("region")) {
                // This was already handled and put to layers.
                continue;
            }
            StatisticalIndicatorDataDimension selector = new StatisticalIndicatorDataDimension(key);
            JSONArray valuesJSON = jsonObject.getJSONObject(key).getJSONArray("values");
            for (int i = 0; i < valuesJSON.length(); i++) {
                selector.addAllowedValue(valuesJSON.getString(i));
            }

            if (selector.getAllowedValues().size() > 0) {
                // Sotka has many indicators with empty allowed values for "sex" for example.
                selectors.addDimension(selector);
            }
        }
        return selectors;
    }
/*
Parsing Sotkanet metadata/JSON like this:
{
	"id": 4,
	"data-updated": "2016-11-02",
	"range": {
		"start": 1996,
		"end": 2015
	},
	"title": {
		"fi": "Mielenterveyden häiriöihin sairaalahoitoa saaneet 0 - 17-vuotiaat / 1 000 vastaavanikäistä",
		"en": "Hospital care for mental disorders, recipients aged 0-17 per 1000 persons of the same age",
		"sv": "0 - 17-åringar som vårdats på sjukhus för psykiska störningar / 1 000 i samma åldrar"
	},
	"description": {
		"fi": "Indikaattori ilmaisee vuoden aikana päädiagnoosilla mielenterveyden häiriöt (F10 - F99, pois lukien F70 - F79 älyllinen kehitysvammaisuus) sairaalahoidossa olleiden 0 - 17-vuotiaiden osuuden tuhatta vastaavanikäistä kohti. Väestötietona käytetään keskiväkilukua. Sairaalahoito sisältää sekä julkisen sektorin (kunnat, kuntayhtymät ja valtio) että yksityisen sektorin järjestämän sairaalahoidon.<p id=suhteutus>Väestösuhteutus on tehty THL:ssä käyttäen Tilastokeskuksen Väestötilaston tietoja.</p> ",
		"en": "The indicator gives the number of those aged 0-17 who have received hospital care with the primary diagnosis of mental disorders (F10-F99, excluding F70-F79 mental retardation) per thousand persons of the same age. Population figures refer to mean population. The indicator covers hospital care in the public sector (municipalities, joint municipal boards and the state), as well as in private sector hospitals.<p id=suhteutus>Population proportions are calculated at THL based on the Population Statistics of Statistics Finland.</p> ",
		"sv": "Indikatorn visar andelen 0 - 17-åringar med huvuddiagnosen psykiska störningar (F10 - F99, med undantag av F70 - F79 psykisk utvecklingsstörning) som under året vårdats på sjukhus per 1 000 i samma åldrar. Medelfolkmängden används som befolkningsuppgift. Sjukhusvården omfattar sjukhusvård som anordnats både av den offentliga sektorn (kommunerna, samkommunerna och staten) och den privata sektorn.<p id=suhteutus>THL har relaterat uppgifterna till hela befolkningen på basis av uppgifterna i statistikcentralens befolkningsstatistik.</p> "
	},
	"interpretation": {
		"fi": "Mielenterveyden häiriöiden vuoksi sairaalahoitoa tarvinneiden lasten ja nuorten määrä suhteutettuna ko. ikäluokkaan kuvaa osaltaan lasten ja nuorten psyykkisten ongelmien määrää ja niiden vaikeusastetta. On kuitenkin huomattava, että sairaalahoidon määrä riippuu myös käytettävissä olevista resursseista (esim. miten paljon lasten ja nuorten psykiatrista sairaalahoitoa ja/tai avohoitoa alueella on tarjolla) sekä vallitsevista hoitokäytännöistä. ",
		"en": "The number of children and young people who have received hospital care for mental disorders in relation to the total population of the same age helps describe the level and severity of mental disorders among children and young people. However, it should be noted that the level of hospital care also depends on the resources available (e.g. the extent to which psychiatric hospital care/outpatient care for children and young people is available in the area) and the treatment practices in use. ",
		"sv": "Antalet barn och unga som på grund av psykiska störningar har varit i behov av sjukhusvård i relation till sin åldersklass visar omfattningen av barns och ungas psykiska problem samt hur svåra problemen är. Man bör dock observera att sjukhusvårdsvolymen också påverkas av de resurser som finns att tillgå (t.ex. hur mycket psykiatrisk sjukhusvård och/eller öppenvård för unga vuxna som tillhandahålls i regionen) samt av den rådande vårdpraxisen. "
	},
	"limits": {
		"fi": "Hoitoilmoitusrekisterin yksilötasoiset tiedot ovat salassa pidettäviä. Tietosuojan vuoksi alle viiden tapauksen kuntakohtaisia tietoja ei julkisteta. ",
		"en": "Individual level data in the Care Registers for Social Welfare and Health Care are confidential. For the sake of privacy protection, no municipality level data with less than five cases are published. ",
		"sv": "Uppgifterna om enskilda personer i Vårdanmälningsregistret är konfidentiella. Om antalet fall i kommunen är färre än fem, publiceras inte uppgifterna på grund av datasekretessen. "
	},
	"legislation": {
		"fi": "Mielenterveyslaki 14.12.1990/1116 sekä Mielenterveysasetus 21.12.1990/1247 / 2a luku. Lasten ja nuorten mielenterveyspalvelut (28.12.2000/1282).<br />Asetuksen 6a§:ssä määritellään mm. hoitolähetteen arvioinnin ja hoidon järjestämisen enimmäisajat (3 viikkoa ja 3 kk). ",
		"en": "Mental Health Act 14.12.1990/1116 and Mental Health Decree 21.12.1990/1247 / Chapter 2a. Mental health services for children and young people (28.12.2000/1282). Section 6a of the Decree defines, among other things, maximum time periods for the assessment of referral to treatment and the arrangement of care (3 weeks and 3 months). ",
		"sv": "Mentalvårdslag 14.12.1990/1116 samt mentalvårdsförordning 21.12.1990/1247/ 2a kap. Mentalvårdstjänster för barn och unga (28.12.2000/1282).<br />I 6a § i förordningen anges bl.a. tiderna för bedömning av remisser och anordnande av vård (3 veckor och 3 månader). "
	},
	"notices": {
		"fi": " ",
		"en": "",
		"sv": ""
	},
	"primaryValueType": {
		"code": "PER1000",
		"title": {
			"fi": "Tuhatta asukasta kohden",
			"en": "Per 1 000 inhabitants",
			"sv": "Per tusen invånare"
		}
	},
	"decimals": 1,
	"classifications": {
		"sex": {
			"title": {
				"fi": "miehet, naiset, yhteensä",
				"en": "male, female, combined",
				"sv": "män, kvinnor, totalt"
			},
			"values": ["male", "female", "total"]
		},
		"age": {
			"title": {
				"fi": "0 - 17-vuotiaat",
				"en": "Ages 0-17",
				"sv": "0-17 år"
			},
			"values": []
		},
		"region": {
			"title": {
				"fi": "Kunta, seutukunta, maakunta, aluehallintoviraston alue, suuralue, Manner-Suomi/Ahvenanmaa, sairaanhoitopiiri, erityisvastuualue, koko maa",
				"en": "Municipality, sub-region, region, area for the regional state administrative agency, major region, Mainland Finland/Åland, hospital district, university hospital special responsibility area, whole country",
				"sv": "Kommun, ekonomisk region, landskap, området för regionförvaltningsverket, storområde, Fastlandsfinland/Åland, sjukvårdsdistrikt, specialupptagningsområde, hela landet"
			},
			"values": ["Kunta", "Maakunta", "Erva", "Aluehallintovirasto", "Sairaanhoitopiiri", "Maa", "Suuralue", "Seutukunta", "Nuts1"]
		}
	},
	"organization": {
		"id": 2,
		"title": {
			"fi": "Terveyden ja hyvinvoinnin laitos (THL)",
			"en": "Institute for Health and Welfare (THL)",
			"sv": "Institutet för hälsa och välfärd (THL)"
		}
	},
	"subjects": [{
		"fi": "psykiatrian laitoshoito",
		"sv": "psykiatrisk institutionsvård",
		"en": "psychiatric inpatient care"
	}, {
		"fi": "psykiatria",
		"sv": "psykiatri",
		"en": "psychiatry"
	}, {
		"fi": "sairaalahoito",
		"sv": "sjukhusvård",
		"en": "hospital care",
		"uri": ""
	}, {
		"fi": "mielenterveys",
		"sv": "psykisk hälsa",
		"en": "mental health",
		"uri": "http://www.yso.fi/onto/yso/p1949"
	}],
	"sources": [{
		"organization": {
			"id": 2,
			"title": {
				"fi": "Terveyden ja hyvinvoinnin laitos (THL)",
				"en": "Institute for Health and Welfare (THL)",
				"sv": "Institutet för hälsa och välfärd (THL)"
			}
		},
		"title": {
			"fi": "Perusterveydenhuoltotilasto (THL)",
			"en": "Primary health care (THL)",
			"sv": "Primär vård (THL)"
		},
		"description": {
			"fi": "Perusterveydenhuollon tilasto perustuu vuosittain terveydenhuollon toimintayksiköistä henkilötunnuksella kerättäviin hoitoilmoituksiin. Valtakunnallinen sosiaali- ja terveydenhuollon hoitoilmoitusjärjestelmä on laajentunut kattamaan perusterveydenhuollon avohoidon vuonna 2011. Perusterveydenhuollon avohoidon hoitoilmoitukset sisältävät tietoja palvelutapahtuman tuottajista, asiakkaiden yhteydenotoista, hoidon tarpeen arvioinnista, ajanvarataustiedoista sekä palvelutapahtuman sisällöstä, kuten käyntisyistä ja toiminnoista sekä jatkohoidosta. Lisäksi perusterveydenhuollon tilastoon tulee tietoja terveyskeskusten laitoshoidosta. Perusterveydenhuollon laitoshoidon hoitoilmoitukset sisältävät tiedot palvelun tuottajasta, potilaan kotikunnasta, hoitoon tulon tiedot, hoitoon liittyvät tiedot toimenpiteineen sekä hoidon päättymiseen liittyviä tietoja.<br><br><br />THL keräsi terveyskeskuksilta vuoteen 2010 asti vuosittain tiedot niiden tuottamista avohoidon palveluista Notitia tiedonkeruulla. Terveyskeskukset ilmoittivat tiedot jäsenkunnittain. Lääkärikäynnit ja muun ammattihenkilökunnan käynnit kysyttiin toiminnoittain, minkä lisäksi kysyttiin lääkärikäynnit ja lääkärillä käyneet potilaat ikäryhmittäin. Suun terveydenhuollon käynneistä kerättiin ikäryhmittäiset käyntitiedot hammaslääkäri-, hammashuoltaja- ja -hoitajakäynneistä, sekä ikäryhmittäiset tiedot suun terveydenhuollon potilaista. THL on kerännyt tiedot vuodesta 2002 lähtien. Suomen Kuntaliitto keräsi vastaavia tietoja vuosilta 1994-2001. Ahvenanmaan tiedot ovat saatavissa vuodesta 2011 lähtien.",
			"en": "The statistics on primary health care are based on care notifications submitted by health care units. In 2011, the national Care Registers for Social Welfare and Health Care were extended to cover also outpatient visits in primary health care. Care notifications concerning outpatient visits in primary health care contain data on service providers, clients' contacts with service providers, assessments of the need for treatment, appointments as well as content of the service event, such as reasons for visit and functions, further treatment, and reasons for health care visits. In addition, the statistics on primary health care contain data on inpatient care in health centres. Care notifications concerning inpatient care in primary health care contain data on service provider and patient's municipality of residence as well as on admission, treatment, and discharge. <br /><BR><BR>Until 2010, THL collected annual data on outpatient care in health centres by using the Notitia data collection. The data reported by health centres was broken down by participating municipality. Physician visits and other practitioner visits were broken down by activity, and physician visits and patients seen by physicians by age group. As for oral health care visits, data were gathered on dentist visits and dental hygienist/assistant visits by age group, and on oral health care patients by age group. THL has collected the data since 2002. Between 1994 and 2001, similar data were collected by the Association of Finnish Local and Regional Authorities. Data from Åland are available from 2011 onwards.",
			"sv": "Statistiken över primärvården bygger på de vårdanmälningar som årligen samlas in från verksamhetsenheterna inom hälso- och sjukvården utifrån personbeteckningen. Det landsomfattande vårdanmälningssystemet för socialvården och hälso- och sjukvården utvidgades till att omfatta den öppna primärvården år 2011. Vårdanmälningarna inom den öppna primärvården innehåller uppgifter om producenterna av servicehändelsen, klienternas kontakttaganden, bedömningen av vårdbehovet, tidsbeställningsinformationen och innehållet i servicehändelsen, såsom orsakerna till besöket, åtgärderna och eftervården. Dessutom innehåller statistiken om primärvården uppgifter om institutionsvården vid hälsovårdscentralerna. Vårdanmälningarna inom den slutna primärvården innehåller uppgifter om serviceproducenten, patientens hemkommun, information om intagningen för vård, uppgifter om vården och information om utskrivningen. <br /><BR><BR>Fram till år 2010 samlade THL årligen in uppgifter av hälsovårdscentralerna om de öppenvårdstjänster som dessa producerat. Uppgifterna samlades in med hjälp av Notitia-metoden. Hälsovårdscentralerna lämnade in uppgifterna enligt medlemskommun. Läkarbesök och besök hos annan personal efterfrågades indelade efter verksamhetstyp. Dessutom efterfrågade man uppgifter efter åldersgrupp om antal läkarbesök och patienter som besökt läkare. När det gäller uppgifter om munhälsovårdsbesök, samlade man in uppgifter indelade efter åldersgrupp om besök hos tandläkare, tandhygienist och tandskötare. Dessutom samlade man in uppgifter efter åldersgrupp om patienterna inom munhälsovården. THL har samlat in dessa uppgifter sedan år 2002. Finlands Kommunförbund samlade in motsvarande uppgifter 1994 - 2001. Ålands uppgifter finns att tillgå från och med 2011.<br />"
		}
	}, {
		"organization": {
			"id": 2,
			"title": {
				"fi": "Terveyden ja hyvinvoinnin laitos (THL)",
				"en": "Institute for Health and Welfare (THL)",
				"sv": "Institutet för hälsa och välfärd (THL)"
			}
		},
		"title": {
			"fi": "Erikoissairaanhoitotilasto (THL)",
			"en": "Erikoissairaanhoitotilasto (THL)",
			"sv": "Erikoissairaanhoitotilasto (THL)"
		},
		"description": {
			"fi": "Erikoissairaanhoidon tilasto perustuu vuosittain terveydenhuollon toimintayksiköistä henkilötunnuksella kerättäviin hoitoilmoituksiin. Ne sisältävät tiedot palvelun tuottajasta, potilaan kotikunnasta, hoitoon tulon tiedot, hoitoon liittyvät tiedot toimenpiteineen sekä hoidon päättymiseen liittyviä tietoja. Psykiatrian erikoisalojen potilaista ilmoitetaan näiden tietojen lisäksi psykiatrian erikoisalojen lisätiedot, jotka käsittelevät lääkehoitoa, pakkotoimia ja psyykkisen tilan arviointia. Samoin vaativista sydänpotilaista kerätään lisätietoja. Lisäksi vuoden viimeisenä päivänä sairaalassa vuodeosastoilla olevista potilaista tehdään potilaslaskenta. <br><br><br />Terveyden ja hyvinvoinnin laitos kerää vuosittain terveydenhuollon palveluntuottajilta hoitoilmoitukset (HILMO) päättyneistä hoitojaksoista ja avohoitokäynneistä. Erikoissairaanhoidon tilastoon on kerätty tietoja kaikista kuntien, kuntayhtymien ja valtion sairaaloista sekä suurimmista yksityissairaaloista. Lisäksi vuoden viimeisenä päivänä sairaalassa olevista potilaista tehdään potilaslaskenta. Nykyisen kaltainen tiedonkeruu laitoshoidosta alkoi terveydenhuollossa vuonna 1994. Erikoissairaanhoidon avohoidosta tietoja on kerätty vuodesta 1998 alkaen, mutta vertailukelpoisia tiedot ovat vuodesta 2006 lähtien. Jo ennen tätä, vuodesta 1967 lähtien, kerättiin ns. poistoilmoitusrekisteriin tietoja sairaaloiden ja terveyskeskusten vuodeosastoilta poistuneista potilaista.",
			"en": "The statistics on specialised health care are based on care notifications submitted by health care units and retrieved on the basis of the unique personal identity number. Care notifications contain data on service provider and patient's municipality of residence as well as information on admission, treatment, procedures and discharge. For psychiatric patients, the data also include additional data on psychiatric specialties, including data on drug therapies, coercive measures and psychiatric evaluations. Additional data are also collected for patients with advanced cardiac condition. Also, a count is taken of patients in hospital wards on the last day of the year. <br /><BR><BR><br />The National Institute for Health and Welfare collects care notifications (HILMO data) from health service providers concerning concluded periods of care and outpatient visits. The statistics on specialised health care include data on all municipalities, joint municipal authorities, state hospitals and largest private hospitals. Also, a count is taken of patients in inpatient care on the last day of the year. In its current form, the data collection on inpatient health care started in 1994. Data on specialised outpatient care have been collected since 1998, but the figures are comparable only from 2006 onwards. Even before that, since 1967, data on patients discharged from hospital and health-centre wards were collected for a hospital discharge register.<br />",
			"sv": "Statistiken över den specialiserade sjukvården bygger på de vårdanmälningar som årligen samlas in från verksamhetsenheterna inom hälso- och sjukvården utifrån personbeteckningen. Vårdanmälningarna innehåller uppgifter om serviceproducenten, patientens hemkommun, information om intagningen för vård, uppgifter om vården och åtgärderna samt information om utskrivningen. Beträffande patienterna inom de psykiatriska specialiteterna lämnas utöver dessa uppgifter även tilläggsuppgifter gällande de psykiatriska specialiteterna. Dessa uppgifter gäller läkemedelsbehandling, tvångsåtgärder och patientens psykiska tillstånd. Likaså samlar man in tilläggsuppgifter om krävande hjärtpatienter. Dessutom genomförs en inventering av alla patienter som finns på vårdavdelningarna på sjukhuset på årets sista dag. <br /><br><br><br />Institutet för hälsa och välfärd samlar varje år in vårdanmälningar (HILMO) av hälso-och sjukvårdsproducenterna. Vårdanmälningarna innehåller uppgifter om avslutade vårdperioder och öppenvårdsbesök. Till statistiken över den specialiserade sjukvården har man samlat in data från alla kommuners och samkommuners sjukhus, de statliga sjukhusen och de största privata sjukhusen. Dessutom genomförs en inventering av alla patienter som finns på sjukhuset på årets sista dag. En datainsamling gällande institutionsvården som motsvarar den nuvarande påbörjades inom hälso- och sjukvården år 1994. Uppgifter om öppenvården inom den specialiserade sjukvården har samlats in sedan år 1998, men uppgifterna är jämförbara från och med år 2006. Redan dessförinnan, med början år 1967, insamlades för det s.k. utskrivningsregistret uppgifter om patienter som skrivits ut från sjukhusens och hälsovårdscentralens vårdavdelningar."
		}
	}],
	"groups": [200, 303, 730, 149, 103]
}
 */
    private void setupMetadata(StatisticalIndicator ind) throws APIException, JSONException {

        // fetch data
        SotkaRequest specificIndicatorRequest = SotkaRequest.getInstance(IndicatorMetadata.NAME);
        specificIndicatorRequest.setBaseURL(config.getUrl());
        specificIndicatorRequest.setIndicator(ind.getId());
        String metadata = specificIndicatorRequest.getData();
        JSONObject json = new JSONObject(metadata);

        // parse the data
        // Later on we might want to add information about the "interpretation", "limits", "legislation", and source "description" also here.
        if (json.has("description")) {
            ind.setDescription(toLocalizationMap(json.getJSONObject("description")));
        }
        if (json.has("range")) {
            JSONObject range = json.getJSONObject("range");
            int start = range.getInt("start");
            int end = range.getInt("end");

            // TODO: Update this before the year 3000. Validating to prevent a DOS attack using insane numbers.
            if (start < 1000 || end > 3000) {
                LOG.warn("Year range doesn't make sense, ignoring");
                return;
            }
            List<String> allowedYears = new ArrayList<>();
            for (int year = start; year <= end; year++) {
                allowedYears.add(String.valueOf(year));
            }
            StatisticalIndicatorDataDimension yearSelector = new StatisticalIndicatorDataDimension("year", allowedYears);
            ind.getDataModel().addDimension(yearSelector);
        }
    }

    private Map<String, String> toLocalizationMap(JSONObject json) throws JSONException {
        Map<String, String> localizationMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        Iterator<String> names = json.keys();
        while (names.hasNext()) {
            String key = names.next();
            localizationMap.put(key, json.getString(key));
        }
        return localizationMap;
    }
}
