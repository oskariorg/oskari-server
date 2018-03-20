
# Sample parser config for complex WFS-layer schema

WFS-layer seed parser configs for wfs 2.0.0 parsers. 
Stored WFS-layer parser configs are in portti_wfs_template_model table.

```
INSERT INTO oskari_wfs_parser_config ( name, type, request_template, response_template, parse_config) VALUES ( 
 'default', 'Default Path', '/fi/nls/oskari/fe/input/request/wfs/generic/ELF_generic_wfs_template.xml',
 'fi.nls.oskari.eu.elf.recipe.universal.ELF_wfs_DefaultParser',
 '{
    "scan": {
      "scanNS": "http://www.opengis.net/wfs/2.0",
      "name": "member"
    },
    "root": {
      "rootNS": "[via application]",
      "name": "[via application]"
    },
    "paths": [
      {
        "path": "[via application]",
        "type": "String",
        "label": "id"
      }
    ]
  }');
```