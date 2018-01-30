
INSERT INTO portti_bundle (name, startup) 
       VALUES ('elf-language-selector','{}');

UPDATE portti_bundle set startup = '{
    "title" : "ELF language selector",
    "fi" : "elf-language-selector",
    "sv" : "elf-language-selector",
    "en" : "elf-language-selector",
    "bundlename" : "elf-language-selector",
    "bundleinstancename" : "elf-language-selector",
    "metadata" : {
        "Import-Bundle" : {
            "elf-language-selector" : {
                "bundlePath" : "/Oskari/packages/elf/bundle/"
            }
        },
        "Require-Bundle-Instance" : []
    },
    "instanceProps" : {}
}' WHERE name = 'elf-language-selector';
