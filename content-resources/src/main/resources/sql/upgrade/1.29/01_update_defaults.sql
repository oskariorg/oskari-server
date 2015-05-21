
-- Update userguide default config
UPDATE portti_bundle set config = '{
    "tags" : "userguide",
    "flyoutClazz" : "Oskari.mapframework.bundle.userguide.SimpleFlyout"
}' WHERE name = 'userguide';

-- Update printout default config
UPDATE portti_bundle set config = '{
    "backendConfiguration": {
        "formatProducers": {
            "image/png": "/?action_route=GetPreview&format=image/png&",
            "application/pdf": "/?action_route=GetPreview&format=application/pdf&"
        }
    }
}' WHERE name = 'printout';