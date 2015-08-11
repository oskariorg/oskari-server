-- Add routingService bundle to portti_bundle_table
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
	'routingService',
	'{}',
	'{}',
	'{
	    "title" : "routingService",
	    "fi" : "routingService",
	    "sv" : "routingService",
	    "en" : "routingService",
	    "bundlename" : "routingService",
	    "bundleinstancename" : "routingService",
	    "metadata" : {
		"Import-Bundle" : {
		    "routingService" : {
				"bundlePath" : "/Oskari/packages/framework/bundle/"
		    }
		},
		"Require-Bundle-Instance" : []
	    },
	    "instanceProps" : {}
	}'
);