package org.oskari.control.myfeatures;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.servlet.http.HttpServletRequest;

import fi.nls.oskari.control.*;
import org.oskari.log.AuditLog;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.map.userlayer.input.FeatureCollectionParser;
import org.oskari.map.userlayer.input.FeatureCollectionParsers;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldType;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

/**
 * ImportMyFeatures allows users to upload a collection of features to be stored in the system.
 *
 * The uploaded file must be a ZIP file and must contain a set of valid files within.
 * Currently supported file formats are:
 * - GPX ({name}.gpx file must be found within the zip)
 * - KML ({name}.kml)
 * - MIF ({name}.mif)
 * - SHP ({name}.shp)
 * @see org.oskari.map.userlayer.input.FeatureCollectionParsers
 *
 * For some of the file formats (GPX, KML) the coordinate reference system is fixed
 * (both use EPSG:4326,lon,lat coordinates). For MIF and SHP we try to detect the coordinate
 * reference system automatically. If the detection fails (for example there's no .prj file
 * in the SHP case) we use client submitted value ('sourceEpsg' parameter) as a fallback.
 */
@OskariActionRoute("ImportMyFeatures")
public class ImportMyFeaturesHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(ImportMyFeaturesHandler.class);

    private static final String PROPERTY_MYFEATURES_MAX_FILE_SIZE_MB = "myfeatures.max.filesize.mb";
    private static final int MAX_FILES_IN_ZIP = 10;

    private static final Charset[] POSSIBLE_CHARSETS_USED_IN_ZIP_FILE_NAMES = {
            StandardCharsets.UTF_8,
            StandardCharsets.ISO_8859_1,
            Charset.forName("CP437"),
            Charset.forName("CP866")
    };

    private static final String PARAM_SOURCE_EPSG_KEY = "sourceEpsg";
    private static final String KEY_STYLE = "style";
    private static final String KEY_LOCALE = "locale";

    private static final int KB = 1024;
    private static final int MB = 1024 * KB;

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * KB;

    private static final int MAX_RETRY_RANDOM_UUID = 100;

    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    private final DiskFileItemFactory diskFileItemFactory = DiskFileItemFactory.builder().setPath(tempDir).setBufferSize(MAX_SIZE_MEMORY).get();
    private int myFeaturesMaxFileSize = -1;
    private long unzippiedFileSizeLimit = -1;

    private MyFeaturesService myFeaturesService;

    public void setMyFeaturesService(MyFeaturesService myFeaturesService) {
        this.myFeaturesService = myFeaturesService;
    }

    @Override
    public void init() {
        if (myFeaturesService == null) {
            myFeaturesService = OskariComponentManager.getComponentOfType(MyFeaturesService.class);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        if (myFeaturesMaxFileSize == -1) {
            // initialized here to workaround timing issue for reading config from properties
            myFeaturesMaxFileSize = PropertyUtil.getOptional(PROPERTY_MYFEATURES_MAX_FILE_SIZE_MB, 10) * MB;
            unzippiedFileSizeLimit = 15 * myFeaturesMaxFileSize; // Max size of unzipped data, 15 * the zip size
        }

        String sourceEPSG = params.getHttpParam(PARAM_SOURCE_EPSG_KEY);
        List<FileItem> fileItems = getFileItems(params.getRequest());
        SimpleFeatureCollection fc;
        Map<String, String> formParams;
        Set<String> validFiles = new HashSet<>();
        FileItem zipFile = null;
        try {
            CoordinateReferenceSystem sourceCRS = decodeCRS(sourceEPSG);
            CoordinateReferenceSystem targetCRS = myFeaturesService.getNativeCRS();
            zipFile = fileItems.stream()
                    .filter(f -> !f.isFormField())
                    .findAny() // If there are more files we'll get the zip or fail miserably
                    .orElseThrow(() -> new ActionParamsException("No file entries in FormData"));
            log.debug("Using value from field:", zipFile.getFieldName(), "as the zip file");
            Charset cs = determineCharsetForZipFileNames(zipFile);
            validFiles = checkZip(zipFile, cs);
            fc = parseFeatures(zipFile, cs, validFiles, sourceCRS, targetCRS);
            formParams = getFormParams(fileItems);
            log.debug("Parsed form parameters:", formParams);

            MyFeaturesLayer layer = store(fc, params.getUser().getUuid(), formParams);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", zipFile.getName())
                    .withParam("id", layer.getId())
                    .added(AuditLog.ResourceType.MYFEATURES_LAYER);

            writeResponse(params, layer);
        } catch (ImportMyFeaturesException e) {
            if (!validFiles.isEmpty()){ // avoid to override with empty list
                e.addContent(ImportMyFeaturesException.InfoType.FILES, validFiles);
            }
            log.error("User uuid:", params.getUser().getUuid(),
                    "zip:", zipFile == null ? "no file" : zipFile.getName(),
                    "info:", e.getOptions().toString());

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", zipFile.getName())
                    .withMsg(e.getMessage())
                    .errored(AuditLog.ResourceType.MYFEATURES_LAYER);

            throw new ActionParamsException(e.getMessage(), e.getOptions());
        } catch (ActionException e) {
            log.error("User uuid:", params.getUser().getUuid(),
                    "zip:", zipFile == null ? "no file" : zipFile.getName(),
                    "files found ("+ validFiles.size() + ") including:",
                    validFiles.stream().collect(Collectors.joining(",")));
            throw e;
        } finally {
            fileItems.forEach(fileItem -> {
                try {
                    fileItem.delete();
                } catch (IOException e) {
                    log.error("Failed to delete file item", e);
                }
            });
        }
    }

    private Charset determineCharsetForZipFileNames(FileItem zipFile) throws ActionException {
        try {
            for (Charset cs : POSSIBLE_CHARSETS_USED_IN_ZIP_FILE_NAMES) {
                try (InputStream in = zipFile.getInputStream();
                        ZipInputStream zis = new ZipInputStream(in, cs)) {
                    while (zis.getNextEntry() != null) {
                        // Get next
                    }
                    log.debug("Succesfully read zip file names with encoding:", cs.name());
                    return cs;
                } catch (IllegalArgumentException ignore) {
                    log.debug("Failed to read zip file names with encoding:", cs.name());
                }
            }
            throw new ActionException("Failed to decode file names in the zip file");
        } catch (IOException e) {
            throw new ActionException("Unexpected IOException occured", e);
        }
    }

    private CoordinateReferenceSystem decodeCRS(String epsg) throws ImportMyFeaturesException {
        try {
            return epsg == null ? null : CRS.decode(epsg);
        } catch (Exception e) {
            throw new ImportMyFeaturesException("Failed to decode CoordinateReferenceSystem from " + epsg,
                    ImportMyFeaturesException.ErrorType.INVALID_EPSG);
        }
    }

    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            JakartaServletFileUpload upload = new JakartaServletFileUpload(diskFileItemFactory);
            upload.setSizeMax(myFeaturesMaxFileSize);
            return upload.parseRequest(request);
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionException("Failed to read request", e);
        }
    }

    private Set<String> checkZip(FileItem zipFile, Charset cs) throws ActionException, ImportMyFeaturesException {
        Set<String> validFiles = new HashSet<>();
        Set<String> extensions = new HashSet<>();
        Map<String,String> ignored = new HashMap<>();
        try (InputStream in = zipFile.getInputStream();
                ZipInputStream zis = new ZipInputStream(in, cs)) {
            ZipEntry ze;
            int numEntries = 0;
            while ((ze = zis.getNextEntry()) != null) {
                if (++numEntries > MAX_FILES_IN_ZIP) {
                    // safeguard against evil zip files, userlayers shouldn't have this many files in any case
                    throw new ImportMyFeaturesException("Zip: " + zipFile.getName() + " contains too many files", ImportMyFeaturesException.ErrorType.MULTI_FILES);
                }
                String name = checkValidFileName(ze, extensions, ignored);
                if (name != null) {
                    validFiles.add(name);
                }
            }
            checkZipContainsExactlyOneMainFile(extensions);
            return validFiles;
        } catch (ImportMyFeaturesException e) {
            Set <String> mainExtensions = extensions.stream()
                    .filter(FeatureCollectionParsers::hasByFileExt)
                    .collect(Collectors.toSet());
            e.addContent(ImportMyFeaturesException.InfoType.EXT_MAIN, mainExtensions);
            e.addContent(ImportMyFeaturesException.InfoType.FILES, validFiles);
            e.addContent(ImportMyFeaturesException.InfoType.IGNORED, ignored);
            throw e;
        } catch (IOException e) {
            throw new ActionException("Unexpected IOException occured", e);
        }
    }

    private static String checkValidFileName(ZipEntry ze, Set<String> extensions, Map<String,String> ignored) throws ImportMyFeaturesException {
        if (ze.isDirectory()) {
            return null;
        }
        String name = ze.getName();
        if (name.indexOf('.') == 0) {
            ignored.put(name, "hidden");
            log.debug(name, "starts with '.', ignoring");
            return null;
        }
        String ext = getFileExt(name);
        if (ext == null) {
            ignored.put(name, "unknown");
            log.debug(name, "doesn't have non-empty file extension, ignoring");
            return null;
        }
        ext = ext.toLowerCase();
        if (!extensions.add(ext)) {
            throw new ImportMyFeaturesException("Zip contains multiple files with same extension: " + ext,
                    ImportMyFeaturesException.ErrorType.MULTI_EXT);
        }
        log.debug(name, "accepted as valid filename");
        return name;
    }

    private void checkZipContainsExactlyOneMainFile(Set<String> extensions) throws ImportMyFeaturesException {
        long mainFileExtensions = extensions.stream()
                .filter(FeatureCollectionParsers::hasByFileExt)
                .limit(2)
                .count();
        if (mainFileExtensions == 0) {
            throw new ImportMyFeaturesException("Couldn't find valid file for import in zip file",
                    ImportMyFeaturesException.ErrorType.NO_FILE);
        } else if (mainFileExtensions == 2) {
            throw new ImportMyFeaturesException("Found too many valid files for import in zip file",
                    ImportMyFeaturesException.ErrorType.MULTI_MAIN);
        }
    }

    private Map<String, String> getFormParams(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
    }

    private SimpleFeatureCollection parseFeatures(FileItem zipFile,
            Charset cs, Set<String> validFiles,
            CoordinateReferenceSystem sourceCRS,
            CoordinateReferenceSystem targetCRS) throws ImportMyFeaturesException, ActionParamsException {
        File dir = null;
        FeatureCollectionParser parser = null;
        try {
            dir = makeRandomTempDirectory();
            File file = new File(dir, zipFile.getName());
            if (!file.toPath().normalize().startsWith(dir.toPath())) {
                // malicious zip could have getName() as ../things/stuff or other bad path
                throw new ImportMyFeaturesException("Zip contains paths we don't want to follow:" + zipFile.getName(),
                        ImportMyFeaturesException.ErrorType.INVALID_ZIP);
            }

            File mainFile = unZip(zipFile, cs, validFiles, dir);
            parser = getParser(mainFile);
            return parser.parse(mainFile, sourceCRS, targetCRS);
        } catch (ImportMyFeaturesException e) {
            if (parser != null) {
                e.addContent(ImportMyFeaturesException.InfoType.PARSER, parser.getSuffix().toLowerCase());
            }
            throw e;
        } catch (ServiceException e) {
            throw new ActionParamsException (e.getMessage());
        } finally {
            // Clean up
            if (dir != null) {
                deleteDir(dir);
            }
        }
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        // If file is non-empty directory recursive delete contents
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    private File makeRandomTempDirectory() throws ServiceException {
        try {
            File tmpFile = File.createTempFile("temp", null);
            File tmpDir = tmpFile.getParentFile();
            tmpFile.delete();
            for (int i = 0; i < MAX_RETRY_RANDOM_UUID; i++) {
                String randomId = UUID.randomUUID().toString().substring(0, 24);
                File dir = new File(tmpDir, randomId);
                if (dir.exists()) {
                    log.info("Temp directory exists already, trying another");
                    continue;
                }
                if (dir.mkdir()) {
                    return dir;
                } else {
                    throw new ServiceException ("Failed to create temp directory");
                }
            }
            throw new ServiceException ("Failed to create temp directory after max attempts!");
        } catch (IOException e) {
            throw new ServiceException ("Failed to create temp directory");
        }
    }

    private File unZip(FileItem zipFile, Charset cs, Set<String> validFiles, File dir) throws ServiceException {
        try (InputStream in = zipFile.getInputStream();
                ZipInputStream zis = new ZipInputStream(in, cs)) {
            ZipEntry ze;
            File mainFile = null;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                String name = ze.getName();
                if (!validFiles.contains(name)) {
                    continue;
                }
                // Beyond this point all files in the root directory of the zip have a non-empty file extension
                // Also we've checked that no two files share the same file extension
                // Save all the files to $TEMP/{random_uuid_dir}/a.{ext} to eliminate the possibility of illegal characters in the filename
                name = "a" + name.substring(name.lastIndexOf('.'));
                File file = new File(dir, name);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    IOHelper.copy(zis, fos, unzippiedFileSizeLimit);
                }
                if (mainFile == null) {
                    String ext = getFileExt(name);
                    if (FeatureCollectionParsers.hasByFileExt(ext)) {
                        mainFile = file;
                    }
                }
            }
            return mainFile;
        } catch (EOFException e) {
            throw new ServiceException("File too large. " + e.getMessage());
        } catch (IOException e) {
            throw new ServiceException("Failed to unzip file: " + zipFile.getName());
        }
    }

    private static String getFileExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i + 1 == name.length()) {
            return null;
        }
        return name.substring(i + 1);
    }

    private FeatureCollectionParser getParser(File mainFile) {
        String ext = getFileExt(mainFile.getName());
        return FeatureCollectionParsers.getByFileExt(ext);
    }

    private MyFeaturesLayer store(SimpleFeatureCollection fc, String ownerUuid, Map<String, String> formParams) throws ImportMyFeaturesException {
        List<MyFeaturesFieldInfo> fields = fc.getSchema().getAttributeDescriptors().stream()
            .map(x -> attribute(x))
            .filter(opt -> !opt.isEmpty())
            .map(Optional::get)
            .collect(Collectors.toList());
        List<MyFeaturesFeature> features = toFeatures(fc, fields);

        MyFeaturesLayer layer = createLayer(ownerUuid, fields, formParams);
        myFeaturesService.createFeatures(layer.getId(), features);

        return layer;
    }

    private static List<MyFeaturesFeature> toFeatures(SimpleFeatureCollection fc, List<MyFeaturesFieldInfo> fields) {
        List<MyFeaturesFeature> features = new ArrayList<>(fc.size());
        try (SimpleFeatureIterator it = fc.features()) {
            while (it.hasNext()) {
                SimpleFeature f = it.next();
                MyFeaturesFeature myFeature = toFeature(f, fields);
                features.add(myFeature);
            }
        }
        return features;
    }

    private static MyFeaturesFeature toFeature(SimpleFeature f, List<MyFeaturesFieldInfo> fields) {
        MyFeaturesFeature myFeature = new MyFeaturesFeature();
        myFeature.setFid(f.getID());
        myFeature.setGeometry((Geometry) f.getDefaultGeometry());
        myFeature.setProperties(toProperties(f, fields));
        return myFeature;
    }

    private static JSONObject toProperties(SimpleFeature f, List<MyFeaturesFieldInfo> fields) {
        JSONObject properties = new JSONObject();

        return properties;
    }

    private MyFeaturesLayer createLayer(String ownerUuid, List<MyFeaturesFieldInfo> fields, Map<String, String> formParams) {
        JSONObject locale = JSONHelper.createJSONObject(formParams.get(KEY_LOCALE));
        // TODO: Do something with the style
        JSONObject style = JSONHelper.createJSONObject(formParams.get(KEY_STYLE));

        MyFeaturesLayer layer = new MyFeaturesLayer();
        layer.setOwnerUuid(ownerUuid);
        layer.setLayerFields(fields);
        layer.setLocale(locale);
        
        myFeaturesService.createLayer(layer);
        return layer;
    }

    private static Optional<MyFeaturesFieldInfo> attribute(AttributeDescriptor attr) {
        String name = attr.getLocalName();
        Class<?> c = attr.getType().getBinding();
        Optional<MyFeaturesFieldType> type = MyFeaturesFieldType.valueFromBinding(c);
        if (type.isEmpty()) {
            // Reduce the level once we have a better understanding of most common binding classes
            log.info("Ignoring attribute:", name, ", no type match for class:", c.getCanonicalName());
            return Optional.empty();
        }
        return Optional.of(MyFeaturesFieldInfo.of(name, type.get()));
    }

    private void writeResponse(ActionParameters params, MyFeaturesLayer layer) {
        /*
        String mapSrs = params.getHttpParam(ActionConstants.PARAM_SRS);
        JSONObject userLayer = UserLayerDataService.parseUserLayer2JSON(ulayer, mapSrs);
        JSONHelper.putValue(userLayer, "featuresCount", ulayer.getFeatures_count());
        JSONObject permissions = UserLayerHandlerHelper.getPermissions();
        JSONHelper.putValue(userLayer, "permissions", permissions);
        if (ulayer.getFeatures_skipped() > 0) {
            JSONObject featuresSkipped = new JSONObject();
            JSONHelper.putValue(featuresSkipped, "featuresSkipped", ulayer.getFeatures_skipped());
            JSONHelper.putValue(userLayer, "warning", featuresSkipped);
        }
        */
        JSONObject resp = new JSONObject();
        ResponseHelper.writeResponse(params, resp);
    }

}
