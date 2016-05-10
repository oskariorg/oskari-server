-- Add selected-featuredata bundle to portti_bundle_table
INSERT 
INTO portti_bundle
(
	name,
	config,
	state,
	startup
)
VALUES 
(
	'selected-featuredata',
	'{}',
	'{}',
	'{
		"title": "SelectedFeaturedata",
		"fi": "selected-featuredata",
		"bundleinstancename": "selected-featuredata",
		"sv": "selected-featuredata",
		"en": "selected-featuredata",
		"bundlename": "selected-featuredata",
		"metadata": {
			"Import-Bundle": {
				"selected-featuredata": {
					"bundlePath": "/Oskari/packages/framework/bundle/"
				}
			},
			"Require-Bundle-Instance": []
		}
	}'
);