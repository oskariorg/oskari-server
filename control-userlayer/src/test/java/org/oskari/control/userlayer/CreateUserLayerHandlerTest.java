package org.oskari.control.userlayer;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.test.control.JSONActionRouteTest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload2.core.FileItem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.map.userlayer.input.FeatureCollectionParsers;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.map.userlayer.service.UserLayerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateUserLayerHandlerTest extends JSONActionRouteTest {

    @Test
    void isFileIgnored() {
        Assertions.assertFalse(CreateUserLayerHandler.isFileIgnored("test.mif"));
        Assertions.assertFalse(CreateUserLayerHandler.isFileIgnored("some/test.mif"));
        Assertions.assertTrue(CreateUserLayerHandler.isFileIgnored(".test.mif"));
        Assertions.assertTrue(CreateUserLayerHandler.isFileIgnored(".some/test.mif"));
        Assertions.assertTrue(CreateUserLayerHandler.isFileIgnored("some/.test.mif"));
    }

    @Test
    void directUploadFormats() {
        // Single-file formats: can be uploaded directly without ZIP
        assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("gpkg"));
        assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("GPKG"));
        assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("kml"));
        assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("KML"));
        assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("gpx"));
        assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("json"));
        assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("geojson"));

        // Multi-file formats: must be uploaded in a ZIP (require sidecar files)
        assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("shp"));
        assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("mif"));

        // Non-format files go to the ZIP path
        assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("zip"));
        assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt(null));
        assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("txt"));
    }

    @Test
    void handlePost_directUpload_geojson() throws Exception {
        byte[] fileBytes = getClass().getResourceAsStream("/org/oskari/control/userlayer/geojson.json").readAllBytes();

        ActionParameters params = createActionParams(getLoggedInUser());
        createHandler("test.geojson", fileBytes).handlePost(params);

        JSONObject response = getResponseJSON();
        assertEquals(10, response.getInt("featuresCount"));
    }

    @Test
    void handlePost_zipUpload_geojson() throws Exception {
        byte[] fileBytes = getClass().getResourceAsStream("/org/oskari/control/userlayer/geojson.json").readAllBytes();
        byte[] zipBytes = createZip("test.geojson", fileBytes);

        ActionParameters params = createActionParams(getLoggedInUser());
        createHandler("test.zip", zipBytes).handlePost(params);

        JSONObject response = getResponseJSON();
        assertEquals(10, response.getInt("featuresCount"));
    }

    /**
     * Creates a handler that bypasses HTTP multipart parsing and database storage.
     * store() is overridden to avoid the DB-connected UserLayerDataService static initializer.
     * The stub service is set before init() so init() doesn't create UserLayerDbServiceMybatisImpl.
     */
    private CreateUserLayerHandler createHandler(String filename, byte[] data) throws IOException {
        FileItem item = mockFileItem(filename, data);
        CreateUserLayerHandler handler = new CreateUserLayerHandler() {
            @Override
            protected List<FileItem> getFileItems(HttpServletRequest request) {
                return List.of(item);
            }

            @Override
            protected UserLayer store(SimpleFeatureCollection fc, String uuid, Map<String, String> formParams) throws UserLayerException {
                UserLayer userLayer = new UserLayer();
                userLayer.setFeatures_count(fc.size());
                return userLayer;
            }

            @Override
            protected void writeResponse(ActionParameters params, UserLayer ulayer) {
                JSONObject json = new JSONObject();
                JSONHelper.putValue(json, "featuresCount", ulayer.getFeatures_count());
                ResponseHelper.writeResponse(params, json);
            }
        };
        handler.setUserLayerService(noopUserLayerDbService());
        handler.init();
        return handler;
    }

    private UserLayerDbService noopUserLayerDbService() {
        return new UserLayerDbService() {
            @Override public int insertUserLayerAndData(UserLayer ul, List ul2) { return 0; }
            @Override public int updateUserLayer(UserLayer ul) { return 0; }
            @Override public UserLayer getUserLayerById(long id) { return null; }
            @Override public List getUserLayerByUuid(String uuid) { return List.of(); }
            @Override public void deleteUserLayerById(long id) {}
            @Override public void deleteUserLayer(UserLayer ul) {}
            @Override public void deleteUserLayersByUuid(String uuid) {}
            @Override public int updatePublisherName(long id, String uuid, String name) { return 0; }
            @Override public String getUserLayerExtent(long id) { return null; }
            @Override public int updateUserLayerData(fi.nls.oskari.domain.map.userlayer.UserLayerData d) { return 0; }
            @Override public org.geotools.data.simple.SimpleFeatureCollection getFeatures(int layerId, org.locationtech.jts.geom.Envelope bbox) { return null; }
        };
    }

    private FileItem mockFileItem(String name, byte[] data) throws IOException {
        FileItem item = mock(FileItem.class);
        when(item.isFormField()).thenReturn(false);
        when(item.getName()).thenReturn(name);
        when(item.getFieldName()).thenReturn("file");
        // thenAnswer so each getInputStream() call gets a fresh stream (ZIP path reads it multiple times)
        when(item.getInputStream()).thenAnswer(inv -> new ByteArrayInputStream(data));
        return item;
    }

    private byte[] createZip(String entryName, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(content);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
