INSERT INTO portti_bundle(
    name, config, state, startup
) VALUES(
    'elf-lang-overrides', '{}', '{}', '{
    "title" : "ELF lang overrides",
    "fi" : "elf-lang-overrides",
    "sv" : "elf-lang-overrides",
    "en" : "elf-lang-overrides",
    "bundlename" : "elf-lang-overrides",
    "bundleinstancename" : "elf-lang-overrides",
    "metadata" : {
        "Import-Bundle" : {
            "elf-lang-overrides" : {
                "bundlePath" : "/Oskari/packages/elf/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}');