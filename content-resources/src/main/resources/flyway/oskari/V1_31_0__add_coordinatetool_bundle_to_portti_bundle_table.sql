-- Add coordinatetool bundle to portti_bundle_table
INSERT 
INTO portti_bundle
(
	id,
	name,
	config,
	state,
	startup
)
VALUES 
(
	(SELECT max(id)+1 FROM portti_bundle),
	'coordinatetool',
	'{}',
	'{}',
	'{
	    "title" : "coordinatetool",
	    "fi" : "coordinatetool",
	    "sv" : "coordinatetool",
	    "en" : "coordinatetool",
	    "bundlename" : "coordinatetool",
	    "bundleinstancename" : "coordinatetool",
	    "metadata" : {
		"Import-Bundle" : {
		    "coordinatetool" : {
				"bundlePath" : "/Oskari/packages/framework/bundle/"
		    }
		},
		"Require-Bundle-Instance" : []
	    },
	    "instanceProps" : {}
	}'
);