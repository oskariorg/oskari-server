# Wfs errors and warnings

 This is a list of errors and warnings, which might be send to Oskari frontend in Transport WFS service.

## Output in CHANNEL_ERROR

              "once"           "true"/"false"  If "true", the message is shown only once for the layer
              "message"        Detail message (e.g. Exception.getMessage
              "type"           Job type ("normal", "MapClick", "Highlight", ...) (Omitted, if "channel" in)
              "channel"        Channel to set before job (Omitted, if "type" in)
              "key"            message key for translations in frontend (e.g. "getfeature_payload_failed";
              "level"          "error" tai "warning"  (default "error")
              "layerId"        Layer id
              "reqId"          Request id
              optional:
              "cause"          exception.getCause() message
              "zoomscale"      requested zoom scale for the layer, when "key" is "layer_scale_out_of_range"
              "minscale"       min scale for the layer in transport cache, when "key" is "layer_scale_out_of_range"
              "maxscale"       max scale for the layer in transport cache, when "key" is "layer_scale_out_of_range"

##  output in CHANNEL_FEATURE

The feature data is send normally in this channel, but there are two special cases

             "features":"empty"   No features found in selected map area
             "feature":"max"      Not all features are returned in selected map area
                                  due to wfs max feature count limits

##  output in CHANNEL_STATUS

This channel reports the status of transport job

              "message"        status message (e.g. "started", "completed"
              "type"           Job type ("normal", "MapClick", "Highlight", ...)
              "layerId"        Layer id
              "reqId"          Request id
              "success"
              "success_nop"

## Error and warning cases ("key" values) in CHANNEL_ERROR (default error level is "error")

#### ERROR_COMMON_JOB_FAILURE = "common_job_failure"

     Exception found in transport job process, which not known (catched) inside the job process.

#### ERROR_COMMON_PROCESS_REQUEST_FAILURE = "common_process_request_failure"

    Exception found in transport process before job run, which not known (catched) inside process request.

#### ERROR_SESSION_CREATION_FAILED = "session_creation_failed"

    Transport service could not init session data for the process (may be websocket or redis problem)

#### ERROR_WFSLAYERSTORE_PARSING_FAILED = "WFSLayerStore_parsing_failed"

    Transport service could not parse wfs layer data (e.g. unknown json key (property name))

#### ERROR_SESSIONSTORE_PARSING_FAILED = "sessionstore_parsing_failed"

    Transport service could not parse session data (e.g. Unrecognized values  in map size or tile size)

#### ERROR_LAYER_SCALE_OUT_OF_RANGE = "layer_scale_out_of_range"

    Map scale was not valid for layer. Requested layer scale is out of scale range settings.
    Reason might be the inconsistency in layer cache values (redis) vs data base layer values
    There are extra values on this case in the output of CHANNEL_ERROR
    "zoomscale"      requested zoom scale for the layer
    "minscale"       min scale for the layer in transport cache
    "maxscale"       max scale for the layer in transport cache

#### ERROR_LAYER_ADD_FAILED = "layer_add_failed"

     Transport service failed to add a map layer to the session - invalid params e.g. styleName
     "level":"warning"


#### ERROR_LAYER_REMOVE_FAILED = "layer_remove_failed"

     Transport service failed to remove a map layer from the session - no layerId
     "level":"warning"

#### ERROR_NO_FEATURES_DEFINED = "no_features_defined"

      Transport service could not init highlight job - layer feature ids are not defined for highlight
      "level":"warning"

#### ERROR_SET_LOCATION_FAILED = "set_location_failed"

      Transport service failed to set location data for the job - lacking parameters e.g. zoom, bbox, grid, tiles"
      "level":"warning"

#### ERROR_SET_MAP_SIZE_FAILED = "set_map_size_failed"

      Transport service failed to set map size - lacking params e.g. width, height"
      "level":"warning"

#### ERROR_SET_LAYER_STYLE_FAILED = "set_layer_style_failed"

      Transport service failed to set layer style - lacking params e.g. styleName"
      "level":"warning"

#### ERROR_SET_LAYER_CUSTOMSTYLE_FAILED = "set_layer_customstyle_failed"

      Transport service failed to set custom layer style - lacking params e.g. fill_color, stroke_color, .."
      "level":"warning"

#### ERROR_SET_MAP_CLICK_FAILED = "set_map_click_failed"

      Transport service failed to set map click - lacking params or GeoJson filter init failed"
      "level":"warning"

#### ERROR_SET_MAP_VISIBILITY_FAILED = "set_map_visibility_failed"

      Transport service failed to set layer visibility - layer visibility parameter is not defined"
      "level":"warning"

#### ERROR_SET_FILTER_FAILED = "set_filter_failed"

      Transport service failed to set GeoJson filter - Reading JSON data failed"
      "level":"warning"

#### ERROR_SET_PROPERTY_FILTER_FAILED = "set_property_filter_failed"

      Transport service failed to set property filter - Reading JSON data failed"
      "level":"warning"

#### ERROR_CONFIGURATION_FAILED = "wfs_configuring_layer_failed"

      Layer configurations couldn't be fetched (job validation)

#### ERROR_NO_PERMISSIONS = "wfs_no_permissions"

      Session has no permissions for getting the layer (job validation)

####  ERROR_WFS_IMAGE_PARSING_FAILED = "wfs_image_parsing_failed"

      Image parsing failed for feature highlight  or for image tiles

#### WARNING_GEOMETRY_PARSING_FAILED = "geometry_parsing_failed"

      Geometry parsing of some features failed in the feature collection (unknown geometry property or transformation error)
      "level":"warning"

#### WARNING_SLDSTYLE_PARSING_FAILED = "sldstyle_parsing_failed"

      SDL style parsing failed for the layer (custon or default)
      "level":"warning"

#### ERROR_INVALID_GEOMETRY_PROPERTY = "invalid_geometry_property"

      No geometry property name defined  or no geometry namespace URI defined.

#### ERROR_GETFEATURE_PAYLOAD_FAILED = "getfeature_payload_failed"

     Creation of GetFeature POST request failed. e.g. filter failure, filter bbox transform failure

#### ERROR_GETFEATURE_POSTREQUEST_FAILED = "getfeature_postrequest_failed"

     e.g. HTTP POST request (GetFeature) failed
     (unknown service url, credentials needed, invalid credentials, credentials not needed, ..)
     or
     There is ExceptionReport in Wfs service response
     (e.g. unknown property name, not applicable code in post request)
     ExceptionText is in the detail message in CHANNEL_ERROR output

     example error output
     ====================
     key:"getfeature_postrequest_failed"
     layerId:"9"
     level:"error"
     message:"Illegal property name: vaestoalue:geommm for feature type vaestoalue:kunta_vaki2012"
     once:false
     reqId:22
     type:"mapClick"

#### ERROR_GETFEATURE_ENGINE_FAILED = "getfeature_engine_failed"

     No FeatureEngine available - maybe invalid wfs layer configuration

#### ERROR_FEATURE_PARSING = "features_parsing_failed"

     Geotools parser failures or Jackson parser failures

#### ERROR_REDIS_COMMUNICATION_FAILURE = "redis_communication_failure";

     Getting Jedis connection from the pool failed ( Redis is not running ?)
     Failed to get necessary key


