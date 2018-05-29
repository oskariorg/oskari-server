-- update startup to point to new implementation
UPDATE portti_bundle SET startup='{
	"metadata": {
		"Import-Bundle": {
			"drawtools": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			}
		}
	},
	"bundlename": "drawtools"
}' where name = 'drawtools';

UPDATE portti_bundle SET startup='{
		"metadata": {
			"Import-Bundle": {
				"mapmyplaces": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"maparcgis": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapmodule": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapwfs2": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapwmts": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapstats": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapuserlayers": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"mapanalysis": {
					"bundlePath": "/Oskari/packages/mapping/ol3/"
				},
				"ui-components": {
					"bundlePath": "/Oskari/packages/framework/bundle/"
				},
				"oskariui": {
					"bundlePath": "/Oskari/packages/framework/bundle/"
				},
				"mapfull": {
					"bundlePath": "/Oskari/packages/framework/bundle/"
				}
			}
		},
		"bundlename": "mapfull"
	}' where name = 'mapfull';

UPDATE portti_bundle SET startup='{
  "bundlename": "toolbar",
  "metadata": {
    "Import-Bundle": {
      "toolbar": {
        "bundlePath": "/Oskari/packages/mapping/ol3/"
      }
    }
  }
}' where name = 'toolbar';

UPDATE portti_bundle SET startup='{
  "bundlename": "infobox",
  "metadata": {
    "Import-Bundle": {
      "infobox": {
        "bundlePath": "/Oskari/packages/mapping/ol3/"
      }
    }
  }
 }' where name = 'infobox';

UPDATE portti_bundle SET startup='{
		"metadata": {
			"Import-Bundle": {
				"analyse": {
					"bundlePath": "/Oskari/packages/analysis/ol3/"
				}
			}
		},
		"bundlename": "analyse"
	}' where name = 'analyse';

UPDATE portti_bundle SET startup='{
		"metadata": {
			"Import-Bundle": {
				"featuredata2": {
					"bundlePath": "/Oskari/packages/framework/"
				}
			}
		},
		"bundlename": "featuredata2"
	}' where name = 'featuredata2';

UPDATE portti_bundle SET startup='{
		"metadata": {
			"Import-Bundle": {
				"metadatacatalogue": {
					"bundlePath": "/Oskari/packages/catalogue/"
				}
			}
		},
		"bundlename": "metadatacatalogue"
	}' where name = 'metadatacatalogue';

UPDATE portti_bundle SET startup='{
    "bundlename" : "heatmap",
    "metadata" : {
        "Import-Bundle" : {
            "heatmap" : {
                "bundlePath" : "/Oskari/packages/mapping/ol3/"
            }
        }
    }
	}' where name = 'heatmap';

-- change myplaces2 -> myplaces3
UPDATE portti_view_bundle_seq SET startup = '{
    "bundlename" : "myplaces3",
    "metadata" : {
       "Import-Bundle" : {
          "myplaces3" : {
            "bundlePath" : "/Oskari/packages/framework/bundle/"
          }
       }
    }
  }', config='{}', state='{}', bundle_id = (select id from portti_bundle where name = 'myplaces3')
  where bundle_id = (select id from portti_bundle where name = 'myplaces2');