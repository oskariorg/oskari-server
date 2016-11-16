UPDATE portti_bundle SET startup='{
    "bundlename": "statsgrid",
    "metadata": {
        "Import-Bundle": {
            "statsgrid": {
                "bundlePath": "/Oskari/packages/statistics/"
            }
        }
    }
}', config='{}', state='{}' where name = 'statsgrid';
