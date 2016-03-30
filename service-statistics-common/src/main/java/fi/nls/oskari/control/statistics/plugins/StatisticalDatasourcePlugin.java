package fi.nls.oskari.control.statistics.plugins;

import java.util.List;

import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import org.json.JSONObject;

/**
 * Each statistical datasource plugin encapsulates access to a single external API
 * where statistical indicator data can be fetched and shown in Oskari.
 * 
 * Each datasource has a list of named indicators.
 * These indicators can be for example:
 * - "Perusterveydenhuollon työterveyshuollon lääkärikäynnit / 1 000 15 - 64-vuotiasta"
 * 
 * Each indicator has:
 * - A localized description shown to user.
 * - An ordered set of different granularity layers such as "Kunta", or "Maakunta".
 * - A set of selectors with a localized name and type and a list of allowed values, and their localizations.
 *   These could be for example: "Gender": "Male", "Female", "Other", "All", or "Year": "2010", "2011", ....
 * 
 * Each different granularity layer has:
 * - A reference to a certain map layer and version in use in Oskari.
 *   This map layer version can be the current one in use, or a previous one. Old layers are preserved in Oskari to
 *   show indicators defined for some older sets of municipalities or so.
 * - A table of data indexed by selectors so that Oskari user can select values for selectors and Oskari can then show
 *   the data for a given granularity level.
 *   
 * If in the future the plugin needs to show real-time information, a notification mechanism can be implemented
 * where the plugin notifies Oskari with the plugin name to tell it to fetch the new set of data.
 * Before that, we can pretty much cache all the values using Jedis.
 */
public interface StatisticalDatasourcePlugin {
    /**
     * Returns a list of statistical data indicators, each with several granularity layers.
     * TODO: Implement a hierarchical tree for the indicators.
     * @param user 
     * @return
     */
    public List<? extends StatisticalIndicator> getIndicators(User user);

    /**
     * Hook for setting up components that the handler needs to handle requests
     */
    public void init(StatisticalDatasource source);

    /**
     * Generally true, if the data does not change all the time, for example based on the user doing the query.
     */
    public boolean canCache();
}
