package fi.nls.oskari.geoserver;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

/**
 * Created by SMAKINEN on 1.9.2015.
 */
interface Geoserver {
        @RequestLine("GET /repos/{owner}/{repo}/contributors")
        List<Workspace> contributors(@Param("owner") String owner, @Param("repo") String repo);

    @RequestLine("POST /workspaces.json")
    @Headers("Content-Type: application/json")
    void createWorkspace(Workspace ws);

    @RequestLine("DELETE /workspaces/{name}.json")
    @Headers("Content-Type: application/json")
    void removeWorkspace(@Param("name") String name);

    @RequestLine("PUT /workspaces/default.json")
    @Headers("Content-Type: application/json")
    void setDefault(Workspace ws);


    @RequestLine("POST /namespaces.json")
    @Headers("Content-Type: application/json")
    void createNamespace(Namespace ws);

    @RequestLine("POST /workspaces/{ns}/datastores.json")
    @Headers("Content-Type: application/json")
    void createDBDatastore(DBDatastore ds, @Param("ns") String namespace);

    @RequestLine("POST /workspaces/{ns}/datastores/{ds}/featuretypes.json")
    @Headers("Content-Type: application/json")
    void createFeatureType(FeatureType ds, @Param("ns") String namespace, @Param("ds") String dataStore);

    // Use raw-param: https://github.com/boundlessgeo/gsconfig/pull/94
    @RequestLine("POST /styles.sld?name={name}&raw=true")
    @Headers("Content-Type: application/vnd.ogc.sld+xml") // , Accept: application/vnd.ogc.sld+xml
    @Body("{content}") // Body template is needed so content isn't transformed by Jackson
    void createSLD(@Param("name") final String name, @Param("content") final String content);

    // Use raw-param: https://github.com/boundlessgeo/gsconfig/pull/94
    @RequestLine("POST /workspaces/{ws}/styles.sld?name={name}&raw=true")
    @Headers("Content-Type: application/vnd.ogc.sld+xml") // , Accept: application/vnd.ogc.sld+xml
    @Body("{content}") // Body template is needed so content isn't transformed by Jackson
    void createSLD(@Param("name") final String name, @Param("content") final String content, @Param("ws") final String workspace);

    // XML is simpler here, use it instead of json
    @RequestLine("POST /layers/{layer}/styles.xml")
    @Headers("Content-Type: text/xml")
    @Body("<style><name>{style}</name></style>")
    void linkStyleToLayer(@Param("style") final String stylename, @Param("layer") final String layername);

    // XML is simpler here, use it instead of json
    @RequestLine("POST /layers/{ns}:{layer}/styles.xml")
    @Headers("Content-Type: text/xml")
    @Body("<style><name>{style}</name></style>")
    void linkStyleToLayer(@Param("style") final String stylename, @Param("layer") final String layername, @Param("ns") final String namespace);

    // XML is simpler here, use it instead of json
    @RequestLine("PUT /layers/{ns}:{layer}")
    @Headers("Content-Type: text/xml")
    @Body("<layer><defaultStyle><name>{style}</name></defaultStyle><enabled>true</enabled></layer>")
    void setDefaultStyleForLayer(@Param("style") final String stylename, @Param("layer") final String layername, @Param("ns") final String namespace);
}
