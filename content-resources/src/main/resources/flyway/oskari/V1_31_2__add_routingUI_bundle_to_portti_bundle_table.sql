-- Add routingUI bundle to portti_bundle_table
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
	'routingUI',
	'{}',
	'{}',
	'{
	    "title" : "routingUI",
	    "fi" : "routingUI",
	    "sv" : "routingUI",
	    "en" : "routingUI",
	    "bundlename" : "routingUI",
	    "bundleinstancename" : "routingUI",
	    "metadata" : {
		"Import-Bundle" : {
		    "routingUI" : {
				"bundlePath" : "/Oskari/packages/framework/bundle/"
		    }
		},
		"Require-Bundle-Instance" : []
	    },
	    "instanceProps" : {}
	}'
);