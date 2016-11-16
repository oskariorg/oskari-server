package fi.nls.oskari.pojo;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.transport.MessageParseHelper;
import fi.nls.oskari.transport.TransportService;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles user's session (current state)
 */
public class SessionStore {
	private static final Logger log = LogFactory.getLogger(SessionStore.class);

    /*
     * This uses the Jackson 1.x version since it's used anyway by the current version of CometD.
     * Using Jackson 2.x results in problems with serialization/deserialization.
     * Perhaps needs a custom serializer...
     */
	private static final ObjectMapper mapper = new ObjectMapper();

	public static final String KEY = "Session_";

	private String client; // bayeux session (used in redis key)
	private String session; // liferay session
    private String route; // liferay cluster route id
    private String uuid; // Oskari user id for id
	private String language;
	private String browser;
	private long browserVersion;
	private Location location;
	private Grid grid;
	private Tile tileSize;
	private Tile mapSize;
	private List<Double> mapScales;
	private Map<String, Layer> layers;
	private Coordinate mapClick; // passed parameter - not saved
	private GeoJSONFilter filter; // passed parameter - not saved
    private PropertyFilter propertyFilter; // passed parameter - not saved
	private boolean keepPrevious = false; // passed parameter - not saved
    private boolean geomRequest = false; // passed parameter - geom property returned or not - not saved

	/**
	 * Constructor with defined session key
	 */
	public SessionStore(String client) {
		this.client = client;
		this.session = "";
        this.route = "";
		location = new Location();
		grid = new Grid();
		mapSize = new Tile();
		mapScales = new ArrayList<Double>();
		layers = new HashMap<String, Layer>();
	}

	/**
	 * Constructs object without parameters
	 */
	public SessionStore() {
		this.client = "";
		this.session = "";
        this.route = "";
		location = new Location();
		grid = new Grid();
		mapSize = new Tile();
		mapScales = new ArrayList<Double>();
		layers = new HashMap<String, Layer>();
	}

	/**
	 * Gets session
	 * 
	 * @return session
	 */
	public String getSession() {
		return session;
	}

	/**
	 * Sets session
	 * 
	 * @param session
	 */
	public void setSession(String session) {
		this.session = session;
	}

    /**
     * Gets route
     *
     * @return route
     */
    public String getRoute() {
        return route;
    }

    /**
     * Sets route
     *
     * @param route
     */
    public void setRoute(String route) {
        this.route = route;
    }

	/**
	 * Gets client
	 * 
	 * @return client
	 */
	public String getClient() {
		return client;
	}

	/**
	 * Sets client
	 * 
	 * @param client
	 */
	public void setClient(String client) {
		this.client = client;
	}
	
	/**
	 * Gets language
	 * 
	 * @return language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets language
	 * 
	 * @param language
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

    /**
     * Get cliet user id
     * @return
     */

    public String getUuid() {
        return uuid;
    }

    /**
     * Set client user id
     * @param uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
	 * Gets browser
	 * 
	 * @return browser
	 */
	public String getBrowser() {
		return browser;
	}

	/**
	 * Sets browser
	 * 
	 * @param browser
	 */
	public void setBrowser(String browser) {
		this.browser = browser;
	}

	/**
	 * Sets browser version
	 * @deprecated Information is no longer sent from browser - keeping for compatibility reasons
	 * @param browserVersion
	 */
	private void setBrowserVersion(long browserVersion) {
		this.browserVersion = browserVersion;
	}

	/**
	 * Gets location
	 * 
	 * @return location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Sets location
	 * 
	 * @param location
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * Gets grid
	 * 
	 * @return grid
	 */
	public Grid getGrid() {
		return grid;
	}

	/**
	 * Sets grid
	 * 
	 * @param grid
	 */
	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	/**
	 * Gets tile size
	 * 
	 * @return tile size
	 */
	public Tile getTileSize() {
		return tileSize;
	}

	/**
	 * Sets tile size
	 * 
	 * @param tileSize
	 */
	public void setTileSize(Tile tileSize) {
		this.tileSize = tileSize;
	}
	
	/**
	 * Gets map size
	 * 
	 * @return map size
	 */
	public Tile getMapSize() {
		return mapSize;
	}

	/**
	 * Sets map size
	 * 
	 * @param mapSize
	 */
	public void setMapSize(Tile mapSize) {
		this.mapSize = mapSize;
	}

	/**
	 * Gets map scales
	 * 
	 * @return map scales
	 */
	public List<Double> getMapScales() {
		return mapScales;
	}

	/**
	 * Sets map scales
	 * 
	 * @param mapScales
	 */
	public void setMapScales(List<Double> mapScales) {
		this.mapScales = mapScales;
	}
	
	/**
	 * Gets location
	 * 
	 * @return location
	 */
	public Map<String, Layer> getLayers() {
		return layers;
	}

	/**
	 * Sets layer
	 * 
	 * @param layerId
	 * @param layer
	 */
	public void setLayer(String layerId, Layer layer) {
		this.layers.put(layerId, layer);
	}

	/**
	 * Removes certain layer
	 * 
	 * @param layerId
	 */
	public void removeLayer(String layerId) {
		this.layers.remove(layerId);
	}

	/**
	 * Checks if certain layer is set
	 * 
	 * @param layerId
	 * @return <code>true</code> if layer is found; <code>false</code>
	 *         otherwise.
	 */
	@JsonIgnore
	public boolean containsLayer(String layerId) {
		return this.layers.containsKey(layerId);
	}
	
	/**
	 * Gets map click
	 * 
	 * @return map click
	 */
	@JsonIgnore
	public Coordinate getMapClick() {
		return mapClick;
	}

	/**
	 * Sets map click
	 * 
	 * @param mapClick
	 */
	public void setMapClick(Coordinate mapClick) {
		this.mapClick = mapClick;
	}
	
	/**
	 * Gets filter
	 * 
	 * @return filter
	 */
	@JsonIgnore
	public GeoJSONFilter getFilter() {
		return filter;
	}

    /**
     * Sets filter
     *
     * @param filter
     */
    public void setFilter(GeoJSONFilter filter) {
        this.filter = filter;
    }

	/**
	 * Sets filter
	 * 
	 * @param filter
	 */
	public void setPropertyFilter(PropertyFilter filter) {
		this.propertyFilter = filter;
	}
    /**
     * Gets property filter
     *
     * @return filter
     */
    @JsonIgnore
    public PropertyFilter getPropertyFilter() {
        return propertyFilter;
    }

	/**
	 * Checks if keeping previous in front
	 * 
	 * @return <code>true</code> if kept; <code>false</code>
	 *         otherwise.
	 */
	@JsonIgnore
	public boolean isKeepPrevious() {
		return this.keepPrevious;
	}

	/**
	 * Sets filter
	 * 
	 * @param keepPrevious
	 */
	public void setKeepPrevious(boolean keepPrevious) {
		this.keepPrevious = keepPrevious;
	}

    /**
     *  Is geometry property in feature response
     * @return
     */
    @JsonIgnore
    public boolean isGeomRequest() {
        return geomRequest;
    }

    /**
     * Geometry property in feature response
     * @param geomRequest  true (yes)
     */
    public void setGeomRequest(boolean geomRequest) {
        this.geomRequest = geomRequest;
    }

    /**
	 * Saves into redis
	 * 
	 * @return <code>true</code> if saved a valid session; <code>false</code>
	 *         otherwise.
	 */
	public boolean save() {
        JedisManager.setex(KEY + client, 86400, getAsJSON());
    	return this.isValid();
	}
	
	/**
	 * Transforms object to JSON String
	 * 
	 * @return JSON String
	 */
	@JsonIgnore
	public String getAsJSON() {
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			log.error(e, "JSON Generation failed");
		} catch (JsonMappingException e) {
			log.error(e, "Mapping from Object to JSON String failed");
		} catch (IOException e) {
			log.error(e, "IO failed");
		}
		return null;
	}

	/**
	 * Transforms parameters JSON String to object
	 * 
	 * @param json
	 * @return object
	 */
	@JsonIgnore
	public static SessionStore setParamsJSON(String json) throws IOException {
		SessionStore store = new SessionStore();

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createJsonParser(json);

		String fieldName = null;
		parser.nextToken();
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Configuration is not an object!");
        }
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			fieldName = parser.getCurrentName();
			parser.nextToken();
			if (fieldName == null) {
				break;
			} else if (TransportService.PARAM_ID.equals(fieldName)) {
				parser.getText();
            } else if (TransportService.PARAM_UUID.equals(fieldName)) {
                parser.getText();
			} else if (TransportService.PARAM_DATA.equals(fieldName)) {
				store = parse(parser);
			} else if (TransportService.PARAM_CHANNEL.equals(fieldName)) {
				parser.getText();
			} else {
				throw new IllegalStateException("Unrecognized field '"
						+ fieldName + "'!");
			}
		}
		parser.close();

		return store;
	}

	/**
	 * Transforms JSON String to object
	 * 
	 * @param json
	 * @return object
	 */
	@JsonIgnore
	public static SessionStore setJSON(String json) throws IOException {

		JsonFactory factory = new JsonFactory();
		JsonParser parser = factory.createJsonParser(json);

		parser.nextToken();
		SessionStore store = parse(parser);
		parser.close();

		return store;
	}

	/**
	 * Parser for object's JSON String format
	 * 
	 * @param parser
	 * @return object
	 */
	@JsonIgnore
	private static SessionStore parse(JsonParser parser) throws IOException {
		SessionStore store = new SessionStore();
		Location location = new Location();
		Grid grid = new Grid();
		Tile tileSize = null;
		Tile mapSize = null;
		Layer layer;
		String key = "n/a";

		String fieldName = null;
		String valueName = null;
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Configuration is not an object!");
        }
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			fieldName = parser.getCurrentName();
			parser.nextToken();
			if (fieldName == null) {
				break;
			} else if (TransportService.PARAM_SESSION.equals(fieldName)) {
				store.setSession(parser.getText());
            } else if (TransportService.PARAM_ROUTE.equals(fieldName)) {
                store.setRoute(parser.getText());
			} else if (TransportService.PARAM_CLIENT.equals(fieldName)) {
				store.setClient(parser.getText());
			} else if (TransportService.PARAM_LANGUAGE.equals(fieldName)) {
				store.setLanguage(parser.getText());
            } else if (TransportService.PARAM_UUID.equals(fieldName)) {
                store.setUuid(parser.getText());
			} else if (TransportService.PARAM_BROWSER.equals(fieldName)) {
				store.setBrowser(parser.getText());
			} else if (TransportService.PARAM_BROWSER_VERSION.equals(fieldName)) {
				store.setBrowserVersion(parser.getValueAsLong());
			} else if (TransportService.PARAM_LOCATION.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        valueName = parser.getCurrentName();
                        if (TransportService.PARAM_LOCATION_SRS.equals(valueName)) {
                            location.setSrs(parser.getText());
                        } else if (TransportService.PARAM_LOCATION_BBOX.equals(valueName)) {
                            List<Double> bbox = new ArrayList<Double>();
                            parser.nextToken(); // start array
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                bbox.add(parser.getValueAsDouble());
                            }
                            location.setBbox(bbox);
                        } else if (TransportService.PARAM_LOCATION_ZOOM.equals(valueName)) {
                            location.setZoom(parser.getValueAsLong());
                        } else {
                            throw new IllegalStateException(
                                    "Unrecognized value in location '" + valueName
                                            + " = " + parser.getText() + "' !");
                        }
                    }
                }
				store.setLocation(location);
			} else if (MessageParseHelper.PARAM_GRID.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        valueName = parser.getCurrentName();
                        long value = parser.getValueAsLong();
                        if (MessageParseHelper.PARAM_ROWS.equals(valueName)) {
                            grid.setRows(((Long) value).intValue());
                        } else if (MessageParseHelper.PARAM_COLUMNS.equals(valueName)) {
                            grid.setColumns(((Long) value).intValue());
                        } else if (MessageParseHelper.PARAM_BOUNDS.equals(valueName)) {
                            List<List<Double>> bounds = new ArrayList<List<Double>>();
                            List<Double> bound = null;
                            if(parser.isExpectedStartArrayToken()) {
                                if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                                        if(parser.isExpectedStartArrayToken()) {
                                            bound = new ArrayList<Double>();
                                            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                                                while (parser.nextToken() != JsonToken.END_ARRAY) {
                                                    bound.add(parser.getValueAsDouble());
                                                }
                                            }
                                            bounds.add(bound);
                                        }
                                    }
                                }
                            }
                            grid.setBounds(bounds);
                        } else {
                            throw new IllegalStateException(
                                    "Unrecognized value in grid '" + valueName
                                            + " = " + parser.getText() + "' !");
                        }
                    }
                }
				store.setGrid(grid);
			} else if (TransportService.PARAM_TILE_SIZE.equals(fieldName)) {
				if(parser.getCurrentToken() == JsonToken.START_OBJECT) {
					tileSize = new Tile();
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						valueName = parser.getCurrentName();
						long value = parser.getValueAsLong();
						if (TransportService.PARAM_WIDTH.equals(valueName)) {
							tileSize.setWidth(((Long) value).intValue());
						} else if (TransportService.PARAM_HEIGHT.equals(valueName)) {
							tileSize.setHeight(((Long) value).intValue());
						} else {
							throw new IllegalStateException(
									"Unrecognized value in tileSize '" + valueName
											+ " = " + parser.getText() + "' !");
						}
					}
				}
				store.setTileSize(tileSize);
			} else if (TransportService.PARAM_MAP_SIZE.equals(fieldName)) {
				if(parser.getCurrentToken() == JsonToken.START_OBJECT) {
					mapSize = new Tile();
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						valueName = parser.getCurrentName();
						long value = parser.getValueAsLong();
						if (TransportService.PARAM_WIDTH.equals(valueName)) {
							mapSize.setWidth(((Long) value).intValue());
						} else if (TransportService.PARAM_HEIGHT.equals(valueName)) {
							mapSize.setHeight(((Long) value).intValue());
						} else {
							throw new IllegalStateException(
									"Unrecognized value in mapSize '" + valueName
											+ " = " + parser.getText() + "' !");
						}
					}
				}
				store.setMapSize(mapSize);
			} else if (TransportService.PARAM_MAP_SCALES.equals(fieldName)) {
				List<Double> scales = new ArrayList<Double>();
                if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        scales.add(parser.getValueAsDouble());
                    }
                }
				store.setMapScales(scales);
			} else if (TransportService.PARAM_LAYERS.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        layer = new Layer();
                        parser.nextToken();
                        key = parser.getCurrentName(); //(Long.parseLong(parser.getCurrentName()));
                        layer.setId(key);
                        if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                valueName = parser.getCurrentName();
                                if (TransportService.PARAM_ID.equals(valueName)) {
                                    layer.setId(parser.getText());         // parser.getValueAsLong());
                                } else if (TransportService.PARAM_LAYER_STYLE.equals(valueName)) {
                                    layer.setStyleName(parser.getText());
                                } else if (TransportService.PARAM_LAYER_VISIBLE.equals(valueName)) {
                                    layer.setVisible(parser.getValueAsBoolean());
                                } else {
                                    throw new IllegalStateException(
                                            "Unrecognized value in layers '"
                                                    + valueName + "'!");
                                }
                            }
                        }
                        store.setLayer(key, layer);
                    }
                }
			} else {
				throw new IllegalStateException("Unrecognized field '"
						+ fieldName + "'!");
			}
		}
		return store;
	}

	/**
	 * Gets saved session from redis
	 * 
	 * @param client
	 * @return layer as JSON String
	 */
	@JsonIgnore
	public static String getCache(String client) {
		return JedisManager.get(KEY + client);
	}

    /**
     * Gets saved session from redis
     *
     * @param client
     * @return layer as JSON String
     */
    @JsonIgnore
    public static String getCacheNecessary(String client) {
        return JedisManager.getNecessary(KEY + client);
    }
	

	/**
	 * Checks if session information is valid
	 * 
	 * @return <code>true</code> if is valid; <code>false</code>
	 *         otherwise.
	 */
	@JsonIgnore
	public boolean isValid() {
		if(this.session.length() == 0 || 
				this.language.equals("null") ||
				this.browser.equals("null") ||
				this.tileSize == null ||
				this.mapSize == null ||
				this.mapSize.getWidth() == 0 ||
				this.mapSize.getHeight() == 0 ||
				this.mapScales.isEmpty())
			return false;
		return true;
	}
}
