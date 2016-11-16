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

#### "common_job_failure"

     Exception found in transport job process, which not known (catched) inside the job process.

#### "common_process_request_failure"

    Exception found in transport process before job run, which not known (catched) inside process request.

#### "session_creation_failed"

    Transport service could not init session data for the process (may be websocket or redis problem)

#### "WFSLayerStore_parsing_failed"

    Transport service could not parse wfs layer data (e.g. unknown json key (property name))

#### "sessionstore_parsing_failed"

    Transport service could not parse session data (e.g. Unrecognized values  in map size or tile size)

#### "layer_scale_out_of_range"

    Map scale was not valid for layer. Requested layer scale is out of scale range settings.
    Reason might be the inconsistency in layer cache values (redis) vs data base layer values
    There are extra values on this case in the output of CHANNEL_ERROR
    "zoomscale"      requested zoom scale for the layer
    "minscale"       min scale for the layer in transport cache
    "maxscale"       max scale for the layer in transport cache

#### "layer_add_failed"

     Transport service failed to add a map layer to the session - invalid params e.g. styleName
     "level":"warning"


#### "layer_remove_failed"

     Transport service failed to remove a map layer from the session - no layerId
     "level":"warning"

#### "no_features_defined"

      Transport service could not init highlight job - layer feature ids are not defined for highlight
      "level":"warning"

#### "parameters_not_set"

      No parameters at all for to set any process request

#### "set_process_request_failed"

      Transport service failed to set location data for the job - lacking parameters e.g. zoom, bbox, grid, tiles"
      or
      Transport service failed to set map size - lacking params e.g. width, height"
      or
      Transport service failed to set layer style - lacking params e.g. styleName"
      or
      Transport service failed to set custom layer style - lacking params e.g. fill_color, stroke_color, .."
      or
      Transport service failed to set map click - lacking params or GeoJson filter init failed"
      or
      Transport service failed to set layer visibility - layer visibility parameter is not defined"
      or
      Transport service failed to set GeoJson filter - Reading JSON data failed"
      or
      Transport service failed to set property filter - Reading JSON data failed"

      "level":"warning"

#### "wfs_configuring_layer_failed"

      Layer configurations couldn't be fetched (job validation)

#### "wfs_no_permissions"

      Session has no permissions for getting the layer (job validation)

####  "wfs_image_parsing_failed"

      Image parsing failed for feature highlight  or for image tiles

#### "geometry_parsing_failed"

      Geometry parsing of some features failed in the feature collection (unknown geometry property or transformation error)
      "level":"warning"

#### "sldstyle_parsing_failed"

      SDL style parsing failed for the layer (custon or default)
      "level":"warning"

#### "invalid_geometry_property"

      No geometry property name defined  or no geometry namespace URI defined.

#### "getfeature_payload_failed"

     Creation of GetFeature POST request failed. e.g. filter failure, filter bbox transform failure

#### "getfeature_postrequest_failed"

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

#### "getfeature_engine_failed"

     No FeatureEngine available - maybe invalid wfs layer configuration

#### "features_parsing_failed"

     Geotools parser failures or Jackson parser failures

#### "redis_communication_failure"

     Getting Jedis connection from the pool failed ( Redis is not running ?)
     Failed to get necessary key


