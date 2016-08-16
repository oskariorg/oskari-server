### Oskari Geographical Data Source status check service, using the online Spatineo Serval service

The Oskari Server backend database contains information concerning available geographical data sources,
stored in the ``oskari_maplayer`` database table.

The *Spatineo Serval* SaaS service gathers availability information on various geographical data sources
on the Internet, and makes it available through a custom HTTP web service.

This particular Oskari Service fetches the availability information from Spatineo Serval, and saves it
to the Oskari database.

The new monitoring API requires your portal-ext.properties to contain:

    spatineo.monitoring.key=<Your private access key here!>

Spatineo Serval API documentation:

* https://docs.google.com/a/spatineo.com/file/d/0B7yQv2YAAzWcQmR3cWlhN1BGWUU/edit?usp=drive_web


    oskari@devbox:~$ https -v -f POST monitor.spatineo.com/api/public/availability-1.0 'service[0][type]=WMS' 'service[0][url]=http://kartat.lounaispaikka.fi/ms6/maakuntakaavat/varsinais-suomi/maakuntakaava_ms6' 'service[0][offering]=mk_tunnelit'

    POST /api/public/availability-1.0 HTTP/1.1
    Accept: */*
    Accept-Encoding: gzip, deflate, compress
    Content-Length: 191
    Content-Type: application/x-www-form-urlencoded; charset=utf-8
    Host: monitor.spatineo.com
    User-Agent: HTTPie/0.8.0

    service%5B0%5D%5Btype%5D=WMS&service%5B0%5D%5Burl%5D=http%3A%2F%2Fkartat.lounaispaikka.fi%2Fms6%2Fmaakuntakaavat%2Fvarsinais-suomi%2Fmaakuntakaava_ms6&service%5B0%5D%5Boffering%5D=mk_tunnelit

    HTTP/1.1 200 OK
    Connection: keep-alive
    Content-Length: 415
    Content-Type: application/json; charset=utf-8
    Date: Mon, 20 Oct 2014 08:30:28 GMT
    Server: nginx
    X-Powered-By: Express

    {
        "result": [
            {
                "infoUrl": "http://directory.spatineo.com/service/9195/",
                "status": "OK",
                "week": {
                    "hoursDown": 2.6677925,
                    "hoursMaintenance": 0,
                    "hoursUp": 173.79492694444446
                },
                "year": {
                    "hoursDown": 514.6802619444444,
                    "hoursMaintenance": 0,
                    "hoursUp": 6496.638144444444
                }
            }
        ],
        "status": "OK",
        "version": "1.0"
    }


    oskaridb=> select id, name, url from oskari_maplayer where type = 'wmslayer' order by id;

    oskaridb=> select * from portti_backendstatus order by ts desc limit 10;
        id     |           ts            | maplayer_id | status |                     statusmessage                      | infourl | statusjson
    -----------+-------------------------+-------------+--------+--------------------------------------------------------+---------+------------
     299245323 | 2014-10-20 14:00:05.511 | 255         | ERROR  | Unknown offering katselupalvelu:palvelualueet          |         |
     299245319 | 2014-10-20 14:00:05.511 | 259         | ERROR  | Unknown offering katselupalvelu:tieluokat              |         |
     299245321 | 2014-10-20 14:00:05.511 | 253         | ERROR  | Unknown offering katselupalvelu:lisakaistat            |         |
     299245322 | 2014-10-20 14:00:05.511 | 256         | ERROR  | Unknown offering katselupalvelu:talvinopeusrajoitukset |         |
     299245320 | 2014-10-20 14:00:05.511 | 254         | ERROR  | Unknown offering katselupalvelu:nopeusrajoitukset      |         |
     299245286 | 2014-10-20 14:00:05.436 | 262         | ERROR  | Unknown service                                        |         |
     299245285 | 2014-10-20 14:00:05.436 | 237         | ERROR  | Unknown service                                        |         |
     299245288 | 2014-10-20 14:00:05.436 | 260         | ERROR  | Unknown service                                        |         |
     299245292 | 2014-10-20 14:00:05.436 | 360         | ERROR  | Unknown service                                        |         |
     299245290 | 2014-10-20 14:00:05.436 | 361         | ERROR  | Unknown service                                        |         |
    (10 rows)

        id     |           ts            | maplayer_id | status |                    statusmessage                    |                   infourl                    | statusjson
    -----------+-------------------------+-------------+--------+-----------------------------------------------------+----------------------------------------------+------------
     299245324 | 2014-10-20 14:02:03.881 | 338         | OK     |                                                     | http://directory.spatineo.com/service/9195/  |
     299245325 | 2014-10-20 14:02:03.881 | 339         | OK     |                                                     | http://directory.spatineo.com/service/9192/  |
     299245326 | 2014-10-20 14:00:03.248 | 332         | OK     |                                                     | http://directory.spatineo.com/service/9193/  |
     299245327 | 2014-10-20 14:02:03.881 | 333         | OK     |                                                     | http://directory.spatineo.com/service/9193/  |
     299245328 | 2014-10-20 14:00:03.248 | 330         | OK     |                                                     | http://directory.spatineo.com/service/9193/  |
     299245329 | 2014-10-20 14:00:03.248 | 159         | OK     |                                                     | http://directory.spatineo.com/service/3568/  |
     299245330 | 2014-10-20 14:00:03.248 | 158         | OK     |                                                     | http://directory.spatineo.com/service/9191/  |
     299245331 | 2014-10-20 14:00:03.248 | 331         | OK     |                                                     | http://directory.spatineo.com/service/9193/  |
     299245332 | 2014-10-20 14:02:03.881 | 157         | OK     |                                                     | http://directory.spatineo.com/service/9191/  |
     299245333 | 2014-10-20 14:02:03.881 | 336         | OK     |                                                     | http://directory.spatineo.com/service/9193/  |
