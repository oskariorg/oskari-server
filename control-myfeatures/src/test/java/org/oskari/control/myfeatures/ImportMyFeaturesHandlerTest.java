package org.oskari.control.myfeatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.fileupload2.core.FileItem;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.map.userlayer.input.FeatureCollectionParsers;
import org.oskari.map.userlayer.input.SHPParser;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldType;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.test.control.JSONActionRouteTest;
import jakarta.servlet.http.HttpServletRequest;

public class ImportMyFeaturesHandlerTest extends JSONActionRouteTest {

    @Test
    public void testMappingFeaturesFromSHP() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("kosa_1401_etrs-gk26_a_region.shp").toURI());
        SimpleFeatureCollection sfc = new SHPParser().parse(file, null, CRS.decode("EPSG:3067", true));

        List<MyFeaturesFieldInfo> fields = ImportMyFeaturesHandler.getFields(sfc.getSchema());
        assertEquals(8, fields.size());
        assertEquals(MyFeaturesFieldType.Integer, getFieldType(fields, "ID"));
        assertEquals(MyFeaturesFieldType.Integer, getFieldType(fields, "LAJI"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "LAJIN_SELI"));
        assertEquals(MyFeaturesFieldType.Double, getFieldType(fields, "Z1"));
        assertEquals(MyFeaturesFieldType.Double, getFieldType(fields, "Z2"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "TEKSTI"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "AJ_KAUPUNG"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "AJ_KAUPU00"));

        List<MyFeaturesFeature> myFeatures = ImportMyFeaturesHandler.toFeatures(sfc, fields, -1);
        assertEquals(sfc.size(), myFeatures.size());

        List<String> fieldNames = fields.stream().map(MyFeaturesFieldInfo::getName).toList();

        for (MyFeaturesFeature myFeature : myFeatures) {
            assertNotNull(myFeature.getGeometry());
            assertNotNull(myFeature.getProperties());
            // Original id should be an additional property "fid"
            assertEquals(1 + fields.size(), myFeature.getProperties().length());
        }

        Map<String, MyFeaturesFeature> myFeaturesById = myFeatures.stream()
                .collect(Collectors.toMap(x -> x.getProperties().get(MyFeaturesFieldInfo.FID.getName()).toString(), x -> x));

        // Check that we find matching feature by fid and all of their properties match
        for (SimpleFeatureIterator it = sfc.features(); it.hasNext();) {
            SimpleFeature f = it.next();
            MyFeaturesFeature mf = myFeaturesById.get(f.getID());
            for (String fieldName : fieldNames) {
                Object expected = f.getAttribute(fieldName);
                Object actual = mf.getProperties().get(fieldName);
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testMaxFeatures() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("kosa_1401_etrs-gk26_a_region.shp").toURI());
        SimpleFeatureCollection sfc = new SHPParser().parse(file, null, CRS.decode("EPSG:3067", true));

        int maxFeatures = 5;
        List<MyFeaturesFieldInfo> fields = ImportMyFeaturesHandler.getFields(sfc.getSchema());
        List<MyFeaturesFeature> myFeatures = ImportMyFeaturesHandler.toFeatures(sfc, fields, maxFeatures);
        assertEquals(maxFeatures, myFeatures.size());
    }

    private static MyFeaturesFieldType getFieldType(List<MyFeaturesFieldInfo> fields, String name) {
        return fields.stream().filter(x -> name.equals(x.getName())).findAny().get().getType();
    }

    @Test
    void isFileIgnored() {
        Assertions.assertFalse(ImportMyFeaturesHandler.isFileIgnored("test.mif"));
        Assertions.assertFalse(ImportMyFeaturesHandler.isFileIgnored("some/test.mif"));
        Assertions.assertTrue(ImportMyFeaturesHandler.isFileIgnored(".test.mif"));
        Assertions.assertTrue(ImportMyFeaturesHandler.isFileIgnored(".some/test.mif"));
        Assertions.assertTrue(ImportMyFeaturesHandler.isFileIgnored("some/.test.mif"));
    }

    @Test
    void isDefaultFIDWorks() {
        Assertions.assertTrue(ImportMyFeaturesHandler.isDefaultFID(SimpleFeatureBuilder.createDefaultFeatureId()));
    }

    @Test
    void directUploadFormats() {
        // Single-file formats: can be uploaded directly without ZIP
        Assertions.assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("gpkg"));
        Assertions.assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("GPKG"));
        Assertions.assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("kml"));
        Assertions.assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("KML"));
        Assertions.assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("gpx"));
        Assertions.assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("json"));
        Assertions.assertTrue(FeatureCollectionParsers.hasDirectUploadByFileExt("geojson"));

        // Multi-file formats: must be uploaded in a ZIP
        Assertions.assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("shp"));
        Assertions.assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("mif"));

        // Non-format files go to the ZIP path
        Assertions.assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("zip"));
        Assertions.assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt(null));
        Assertions.assertFalse(FeatureCollectionParsers.hasDirectUploadByFileExt("txt"));
    }

    @Test
    void handlePost_directUpload_geojson() throws Exception {
        byte[] fileBytes = getClass().getResourceAsStream("/org/oskari/control/myfeatures/geojson.json").readAllBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ActionParameters params = createActionParams(getLoggedInUser());
        params.setResponse(mockHttpServletResponse(baos));
        createHandler("test.geojson", fileBytes).handlePost(params);

        JSONObject response = new JSONObject(baos.toString());
        assertNotNull(response.getJSONObject("layer"));
        assertEquals(10, capturedFeatureCount);
    }

    @Test
    void handlePost_zipUpload_geojson() throws Exception {
        byte[] fileBytes = getClass().getResourceAsStream("/org/oskari/control/myfeatures/geojson.json").readAllBytes();
        byte[] zipBytes = createZip("test.geojson", fileBytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ActionParameters params = createActionParams(getLoggedInUser());
        params.setResponse(mockHttpServletResponse(baos));
        createHandler("test.zip", zipBytes).handlePost(params);

        JSONObject response = new JSONObject(baos.toString());
        assertNotNull(response.getJSONObject("layer"));
        assertEquals(10, capturedFeatureCount);
    }

    @Test
    void handlePost_kmzUpload() throws Exception {
        byte[] kmzBytes = getClass().getResourceAsStream("/org/oskari/control/myfeatures/iceland.kmz").readAllBytes();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ActionParameters params = createActionParams(getLoggedInUser());
        params.setResponse(mockHttpServletResponse(baos));
        createHandler("iceland.kmz", kmzBytes).handlePost(params);

        JSONObject response = new JSONObject(baos.toString());
        assertNotNull(response.getJSONObject("layer"));
        Assertions.assertTrue(capturedFeatureCount > 0, "KMZ should contain features");
    }

    private int capturedFeatureCount;

    private ImportMyFeaturesHandler createHandler(String filename, byte[] data) throws IOException {
        FileItem item = mockFileItem(filename, data);
        ImportMyFeaturesHandler handler = new ImportMyFeaturesHandler() {
            @Override
            protected List<FileItem> getFileItems(HttpServletRequest request) {
                return List.of(item);
            }

            @Override
            protected MyFeaturesLayer store(SimpleFeatureCollection fc, String ownerUuid, Map<String, String> formParams) {
                capturedFeatureCount = fc.size();
                MyFeaturesLayer layer = new MyFeaturesLayer();
                layer.setId(UUID.randomUUID());
                layer.setOwnerUuid(ownerUuid);
                layer.setFeatureCount(fc.size());
                return layer;
            }
        };
        handler.setMyFeaturesService(stubService());
        handler.setObjectMapper(new com.fasterxml.jackson.databind.ObjectMapper());
        handler.init();
        return handler;
    }

    private MyFeaturesService stubService() {
        return new MyFeaturesService() {
            @Override public CoordinateReferenceSystem getNativeCRS() {
                try { return CRS.decode("EPSG:4326", true); } catch (Exception e) { throw new RuntimeException(e); }
            }
            @Override public MyFeaturesLayer getLayer(UUID layerId) { return null; }
            @Override public void createLayer(MyFeaturesLayer layer) {}
            @Override public void updateLayer(MyFeaturesLayer layer) {}
            @Override public void deleteLayer(UUID layerId) {}
            @Override public MyFeaturesFeature getFeature(UUID layerId, long featureId) { return null; }
            @Override public void createFeature(UUID layerId, MyFeaturesFeature feature) {}
            @Override public void updateFeature(UUID layerId, MyFeaturesFeature feature) {}
            @Override public void deleteFeature(UUID layerId, long featureId) {}
            @Override public List<MyFeaturesFeature> getFeatures(UUID layerId) { return List.of(); }
            @Override public List<MyFeaturesFeature> getFeaturesByBbox(UUID layerId, double minX, double minY, double maxX, double maxY) { return List.of(); }
            @Override public void deleteFeaturesByLayerId(UUID layerId) {}
            @Override public void createFeatures(UUID layerId, List<MyFeaturesFeature> features) {}
            @Override public List<MyFeaturesLayer> getLayersByOwnerUuid(String ownerUuid) { return List.of(); }
            @Override public void deleteLayersByOwnerUuid(String ownerUuid) {}
            @Override public void swapAxisOrder(UUID layerId) {}
        };
    }

    private FileItem mockFileItem(String name, byte[] data) throws IOException {
        FileItem item = mock(FileItem.class);
        when(item.isFormField()).thenReturn(false);
        when(item.getName()).thenReturn(name);
        when(item.getFieldName()).thenReturn("file");
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
