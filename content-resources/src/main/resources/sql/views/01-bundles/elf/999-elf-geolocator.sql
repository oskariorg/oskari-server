INSERT INTO portti_bundle(
    name, config, state, startup
) VALUES(
    'elf-geolocator', '{}', '{}', '{
    "title" : "ELF geolocator",
    "fi" : "elf-geolocator",
    "sv" : "elf-geolocator",
    "en" : "elf-geolocator",
    "bundlename" : "elf-geolocator",
    "bundleinstancename" : "elf-geolocator",
    "metadata" : {
        "Import-Bundle" : {
            "elf-geolocator" : {
                "bundlePath" : "/Oskari/packages/elf/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}');
