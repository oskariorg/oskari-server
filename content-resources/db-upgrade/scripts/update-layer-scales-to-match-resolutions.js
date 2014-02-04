// SCRIPT=update-layer-scales-to-match-resolutions node app.js

var _ = require("lodash-node");

module.exports = function(client) {
  client.connect(function(err) {
    if(err) {
      return console.error('Could not connect to postgres', err);
    }
    var query = client.query(
    	"SELECT id, minscale, maxscale, locale FROM oskari_maplayer"
    );

    var rowCount = 0;
    var updateCount = 0;
    var finished = false;
    var changeCount = 0;
    query.on("row", function(row) {
      rowCount++;

      var name = getLayerName(row.locale, 'fi');
      var result = checkLayerScales(row.id, name, row.minscale, row.maxscale);
      if(result.modified) {
          changeCount++
          var updateQuery = "UPDATE oskari_maplayer SET minscale=" + result.minscale + ", maxscale=" + result.maxscale +
              " WHERE id =" + row.id;

          client.query(updateQuery, function(err, res) {
              if(err) throw err;

              updateCount++;
              if(updateCount === rowCount && finished) {
                  shutdown(client, rowCount, changeCount);
              }
          });
      }
      else {
          updateCount++;
      }

      if(updateCount === rowCount && finished) {
          shutdown(client, rowCount, changeCount);
      }
    });

    query.on("end", function(row) {
      finished = true;
        if(updateCount === rowCount && finished) {
            shutdown(client, rowCount, changeCount);
        }
    });
  });

  function shutdown(client, count, changeCount) {
      console.log('===================================');
      console.log('Processed layers:', count);
      console.log('Layers requiring change:', changeCount);
      client.end();
  }

  function getLayerName(locale, lang) {
    try {
      var loc = JSON.parse(locale);
      if(loc[lang]) {
        return loc[lang].name;
      }
    }
    catch(e) {
      //console.error('Unable to parse layer name from:', locale);
      return '[parse error]';
    }
    return '<null>';
  }

function checkLayerScales(id, name, minscale, maxscale) {
  // getFirstVisibleZoomLevel - oldScales, newScales
  var minZoom = getFirstVisibleZoomLevel(true, minscale, oldScales);
  var maxZoom = getFirstVisibleZoomLevel(false, maxscale, oldScales);
  var minZoomWithNew = getFirstVisibleZoomLevel(true, minscale, newScales);
  var maxZoomWithNew = getFirstVisibleZoomLevel(false, maxscale, newScales);
  var notification = (minZoom !== minZoomWithNew || maxZoom !== maxZoomWithNew ) ? " CHANGED!!" : '';
  console.log('Layer name:', name, ' [ id:', id, ']', notification);
  console.log('  was visible on zoom levels:   ', minZoom, '-', maxZoom);
  console.log('  is now visible on zoom levels:', minZoomWithNew, '-', maxZoomWithNew);
  var newMinScale = getNewScale(true, minZoom);
  var newMaxScale = getNewScale(false, maxZoom);
/*
  var newMinZoom = getFirstVisibleZoomLevel(true, newMinScale, newScales);
  var maxMaxZoom = getFirstVisibleZoomLevel(false, newMaxScale, newScales);
  console.log('  after change visible on zoom levels:', newMinZoom, '-', maxMaxZoom);
  */
  console.log('  minZoom level should be changed:', (minZoom !== minZoomWithNew));
  console.log('  minscale:', minscale, ' -> ', newMinScale);
  console.log('  maxZoom level should be changed:', (maxZoom !== maxZoomWithNew));
  console.log('  maxscale:', maxscale, ' -> ', newMaxScale);
  console.log('');

  var result = {
      minscale : minscale,
      maxscale : maxscale,
      modified :false
  };
  if(minZoom !== minZoomWithNew) {
      result.minscale = newMinScale;
      result.modified = true;
  }
  if(maxZoom !== maxZoomWithNew) {
      result.maxscale = newMaxScale;
      result.modified = true;
  }
  return result;
}

function getNewScale(isMin, zoomLevel) {
  // min
  if(isMin) {
    if(zoomLevel < 1) {
      // out of bounds, remove scale limitations
      return newScales[0] + 10;
      //return -1;
    }
    else {
      var scale = newScales[zoomLevel];
      var variance = (newScales[zoomLevel -1] - scale) / 2;
      return scale + variance;
    }
  }
  // max
  else {
    if(zoomLevel > (oldScales.length -2)) {
      // out of bounds, remove scale limitations
      /*
      var scale = newScales[oldScales.length -1] - 10;
      if(scale < 1) {
        scale = 1;
      }
      return scale;
      */
      return 1;
    }
    else {
      var scale = newScales[zoomLevel];
      var variance = (scale - newScales[zoomLevel + 1]) / 2;
      return scale - variance;
    }
  }
  throw 'Shouldnt happen :o';
}

// maxscale is the smaller value, minscale is the bigger one
var oldScales = [   5669280,    2834640,    1417320,    566928,    283464,    141732,  56692.8,  28346.4, 11338.56,  5669.28, 2834.64, 1417.32,  708.66];
var newScales = [5805342.72, 2902671.36, 1451335.68, 725667.84, 362833.92, 181416.96, 90708.48, 45354.24, 22677.12, 11338.56, 5669.28, 2834.64, 1417.32];

  function getFirstVisibleZoomLevel(isMin, scale, list) {
    // not limited, return max value
    if(scale === -1) {
      if(isMin) return 0;
      else return (list.length -1);
    }

    // out of scale bounds, return max value
    if(isMin && scale > list[0]) {
      return 0;
    }
    else if(!isMin && scale < list[list.length -1]) {
      return list.length -1;
    }

    for(var zoom = 0; zoom < list.length -1; ++zoom) {
      var match = isBetween(scale, list[zoom], list[zoom +1]);

      if(match) {
        if(isMin) {
          // minscale
          return zoom +1;
        }
        else {
          // maxscale
          return zoom;
        }
      }
    }
  }

  function isBetween(scale, min, max) {
    return (scale > max && scale < min);
  }
}

