### Oskari Geographical Data Source status check service, using the online Spatineo Monitor service

The Oskari Server backend database contains information concerning available geographical data sources,
stored in the ``oskari_maplayer`` database table.

The *Spatineo Monitor* SaaS service gathers availability information on various geographical data sources
on the Internet, and makes it available through a custom HTTP web service.

This particular Oskari Service fetches the availability information from Spatineo Monitor, and saves it
to the Oskari database.

To work the module requires your oskari-ext.properties to contain:

    spatineo.monitoring.url=<The endpoint address of the API>
    spatineo.monitoring.key=<Your private access key here!>