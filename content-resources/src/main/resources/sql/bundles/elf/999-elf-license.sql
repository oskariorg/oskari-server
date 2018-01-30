INSERT INTO portti_bundle(
    name, config, state, startup
) VALUES(
    'elf-license', '{}', '{}', '{
    "title" : "ELF license",
    "fi" : "elf-license",
    "sv" : "elf-license",
    "en" : "elf-license",
    "bundlename" : "elf-license",
    "bundleinstancename" : "elf-license",
    "metadata" : {
        "Import-Bundle" : {
            "elf-license" : {
                "bundlePath" : "/Oskari/packages/elf/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}');