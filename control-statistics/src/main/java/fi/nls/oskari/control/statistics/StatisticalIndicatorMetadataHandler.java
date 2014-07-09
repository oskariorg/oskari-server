package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import static fi.nls.oskari.control.statistics.util.Constants.*;

/**
 * Returns metadata for indicator in statistical datasource registered to Oskari.
 */
@OskariActionRoute("StatisticalIndicatorMetadata")
public class StatisticalIndicatorMetadataHandler extends ActionHandler {
    private final static Logger log = LogFactory.getLogger(StatisticalIndicatorMetadataHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {

        final int datasourceId = params.getRequiredParamInt(PARAM_DATASOURCE);
        final int indicatorId = params.getRequiredParamInt(PARAM_ID);

        // TODO: load indicators metadata based on datasource/indicator
        JSONObject response = JSONHelper.createJSONObject("{\n" +
                "    \"id\" : 74,\n" +
                "    \"title\" : {\n" +
                "        \"fi\": \"Yksinhuoltajaperheet, % lapsiperheistä\",\n" +
                "        \"en\": \"Single parent families, as % of all families with children\",\n" +
                "        \"sv\": \"Familjer med bara en förälder, % av barnfamiljerna\"\n" +
                "    },\n" +
                "    \"description\" : {\n" +
                "        \"fi\": \"Indikaattori ilmaisee yhden huoltajan lapsiperheiden osuuden prosentteina kaikista lapsiperheistä. <br>Lapsiperheiksi luokitellaan perheet, joissa on alle 18-vuotiaita lapsia. Perheen muodostavat yhdessä asuvat avio- tai avoliitossa olevat henkilöt ja heidän lapsensa, jompikumpi vanhemmista lapsineen sekä avio- ja avopuolisot ilman lapsia.\",\n" +
                "        \"en\": \"The indicator gives the percentage of single parent families of all families with children. Families with children refers to families with children under 18. A family consists of a married or cohabiting couple and their children living together; or a parent and his or her children living together; or a married or cohabiting couple without children.\",\n" +
                "        \"sv\": \"Indikatorn visar den procentuella andelen ensamförsörjarfamiljer av alla barnfamiljer. En familj utgörs av gifta eller samboende par och deras barn, av endera föräldern tillsammans med sina barn (ensamförsörjarfamilj) och av gifta eller samboende par utan barn. Barnfamiljer är familjer med minst ett barn under 18 år.\"\n" +
                "    },\n" +
                "    \"organization\" : {\n" +
                "        \"id\" : 3,\n" +
                "        \"title\": {\n" +
                "            \"fi\": \"Tilastokeskus\",\n" +
                "            \"en\": \"Statistics Finland\",\n" +
                "            \"sv\": \"Statistikcentralen\"\n" +
                "        }\n" +
                "    }\n" +
                "}");
        ResponseHelper.writeResponse(params, response);
    }

}
