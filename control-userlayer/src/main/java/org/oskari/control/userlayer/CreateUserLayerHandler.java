package org.oskari.control.userlayer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import fi.nls.oskari.control.*;
import org.oskari.log.AuditLog;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.map.userlayer.input.FeatureCollectionParser;
import org.oskari.map.userlayer.input.FeatureCollectionParsers;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.map.userlayer.service.UserLayerDbServiceMybatisImpl;
import org.oskari.map.userlayer.service.UserLayerException;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.domain.map.UserDataStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

/**
 * CreateUserLayer allows users to upload a collection of features to be stored in the system.
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
@OskariActionRoute("CreateUserLayer")
public class CreateUserLayerHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(CreateUserLayerHandler.class);

    private static final String PROPERTY_USERLAYER_MAX_FILE_SIZE_MB = "userlayer.max.filesize.mb";
    private static final String PROPERTY_TARGET_EPSG = "oskari.native.srs";
    private static final int MAX_FILES_IN_ZIP = 10;

    private static final Charset[] POSSIBLE_CHARSETS_USED_IN_ZIP_FILE_NAMES = {
            StandardCharsets.UTF_8,
            StandardCharsets.ISO_8859_1,
            Charset.forName("CP437"),
            Charset.forName("CP866")
    };

    private static final String PARAM_SOURCE_EPSG_KEY = "sourceEpsg";
    private static final String KEY_NAME = "layer-name";
    private static final String KEY_DESC = "layer-desc";
    private static final String KEY_SOURCE = "layer-source";
    private static final String KEY_STYLE = "layer-style";

    private static final int KB = 1024;
    private static final int MB = 1024 * KB;

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * KB;

    private static final int MAX_RETRY_RANDOM_UUID = 100;

    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);
    private final String targetEPSG = PropertyUtil.get(PROPERTY_TARGET_EPSG, "EPSG:4326");
    private static final int userlayerMaxFileSize = PropertyUtil.getOptional(PROPERTY_USERLAYER_MAX_FILE_SIZE_MB, 10) * MB;
    private static final long FILE_SIZE_LIMIT = 15 * userlayerMaxFileSize; // Max size of unzipped data, 15 * the zip size

    private UserLayerDbService userLayerService;

    public void setUserLayerService(UserLayerDbService userLayerService) {
        this.userLayerService = userLayerService;
    }

    @Override
    public void init() {
        if (userLayerService == null) {
            userLayerService = new UserLayerDbServiceMybatisImpl();
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        String sourceEPSG = params.getHttpParam(PARAM_SOURCE_EPSG_KEY);
        List<FileItem> fileItems = getFileItems(params.getRequest());
        SimpleFeatureCollection fc;
        Map<String, String> formParams;
        Set<String> validFiles = new HashSet<>();
        FileItem zipFile = null;
        try {
            CoordinateReferenceSystem sourceCRS = decodeCRS(sourceEPSG);
            CoordinateReferenceSystem targetCRS = decodeCRS(targetEPSG);
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
            UserLayer userLayer = store(fc, params.getUser().getUuid(), formParams);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", zipFile.getName())
                    .withParam("id", userLayer.getId())
                    .added(AuditLog.ResourceType.USERLAYER);

            writeResponse(params, userLayer);
        } catch (UserLayerException e) {
            if (!validFiles.isEmpty()){ // avoid to override with empty list
                e.addContent(UserLayerException.InfoType.FILES, validFiles);
            }
            log.error("User uuid:", params.getUser().getUuid(),
                    "zip:", zipFile == null ? "no file" : zipFile.getName(),
                    "info:", e.getOptions().toString());

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", zipFile.getName())
                    .withMsg(e.getMessage())
                    .errored(AuditLog.ResourceType.USERLAYER);

            throw new ActionParamsException(e.getMessage(), e.getOptions());
        } catch (ActionException e) {
            log.error("User uuid:", params.getUser().getUuid(),
                    "zip:", zipFile == null ? "no file" : zipFile.getName(),
                    "files found ("+ validFiles.size() + ") including:",
                    validFiles.stream().collect(Collectors.joining(",")));
            throw e;
        } finally {
            fileItems.forEach(FileItem::delete);
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

    private CoordinateReferenceSystem decodeCRS(String epsg) throws UserLayerException {
        try {
            return epsg == null ? null : CRS.decode(epsg);
        } catch (Exception e) {
            throw new UserLayerException("Failed to decode CoordinateReferenceSystem from " + epsg,
                    UserLayerException.ErrorType.INVALID_EPSG);
        }
    }

    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            upload.setSizeMax(userlayerMaxFileSize);
            return upload.parseRequest(request);
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionException("Failed to read request", e);
        }
    }

    private Set<String> checkZip(FileItem zipFile, Charset cs) throws ActionException, UserLayerException {
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
                    throw new UserLayerException("Zip: " + zipFile.getName() + " contains too many files", UserLayerException.ErrorType.MULTI_FILES);
                }
                String name = checkValidFileName(ze, extensions, ignored);
                if (name != null) {
                    validFiles.add(name);
                }
            }
            checkZipContainsExactlyOneMainFile(extensions);
            return validFiles;
        } catch (UserLayerException e) {
            Set <String> mainExtensions = extensions.stream()
                    .filter(FeatureCollectionParsers::hasByFileExt)
                    .collect(Collectors.toSet());
            e.addContent(UserLayerException.InfoType.EXT_MAIN, mainExtensions);
            e.addContent(UserLayerException.InfoType.FILES, validFiles);
            e.addContent(UserLayerException.InfoType.IGNORED, ignored);
            throw e;
        } catch (IOException e) {
            throw new ActionException("Unexpected IOException occured", e);
        }
    }

    private static String checkValidFileName(ZipEntry ze, Set<String> extensions, Map<String,String> ignored) throws UserLayerException {
        if (ze.isDirectory()) {
            return null;
        }
        String name = ze.getName();
        if (name.indexOf('/') >= 0) {
            ignored.put(name, "folder");
            log.debug(name, "is inside a directory, ignoring");
            return null;
        }
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
            throw new UserLayerException("Zip contains multiple files with same extension: " + ext,
                    UserLayerException.ErrorType.MULTI_EXT);
        }
        log.debug(name, "accepted as valid filename");
        return name;
    }

    private void checkZipContainsExactlyOneMainFile(Set<String> extensions) throws UserLayerException {
        long mainFileExtensions = extensions.stream()
                .filter(FeatureCollectionParsers::hasByFileExt)
                .limit(2)
                .count();
        if (mainFileExtensions == 0) {
            throw new UserLayerException("Couldn't find valid file for import in zip file",
                    UserLayerException.ErrorType.NO_FILE);
        } else if (mainFileExtensions == 2) {
            throw new UserLayerException("Found too many valid files for import in zip file",
                    UserLayerException.ErrorType.MULTI_MAIN);
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
            CoordinateReferenceSystem targetCRS) throws UserLayerException, ActionParamsException {
        File dir = null;
        FeatureCollectionParser parser = null;
        try {
            dir = makeRandomTempDirectory();
            File mainFile = unZip(zipFile, cs, validFiles, dir);
            parser = getParser(mainFile);
            return parser.parse(mainFile, sourceCRS, targetCRS);
        }catch (UserLayerException e) {
            if (parser != null) {
                e.addContent(UserLayerException.InfoType.PARSER, parser.getSuffix().toLowerCase());
            }
            throw e;
        }catch (ServiceException e) {
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
                    IOHelper.copy(zis, fos, FILE_SIZE_LIMIT);
                }
                if (mainFile == null) {
                    String ext = getFileExt(name);
                    if (FeatureCollectionParsers.hasByFileExt(ext)) {
                        mainFile = file;
                    }
                }
            }
            return mainFile;
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

    private UserLayer store(SimpleFeatureCollection fc, String uuid, Map<String, String> formParams)
            throws UserLayerException {
            UserLayer userLayer = createUserLayer(fc, uuid, formParams);
            List<UserLayerData> userLayerDataList = UserLayerDataService.createUserLayerData(fc, uuid);
            userLayer.setFeatures_count(userLayerDataList.size());
            userLayer.setFeatures_skipped(fc.size() - userLayerDataList.size());
            userLayerService.insertUserLayerAndData(userLayer, userLayerDataList);
            return userLayer;
    }

    private UserLayer createUserLayer(SimpleFeatureCollection fc, String uuid, Map<String, String> formParams) {
        String name = formParams.get(KEY_NAME);
        String desc = formParams.get(KEY_DESC);
        String source = formParams.get(KEY_SOURCE);
        String style = formParams.get(KEY_STYLE);
        return UserLayerDataService.createUserLayer(fc, uuid, name, desc, source, style);
    }

    private void writeResponse(ActionParameters params, UserLayer ulayer) {
        String mapSrs = params.getHttpParam(ActionConstants.PARAM_SRS);
        JSONObject userLayer = UserLayerDataService.parseUserLayer2JSON(ulayer, mapSrs);

        JSONHelper.putValue(userLayer, "featuresCount", ulayer.getFeatures_count());
        JSONObject permissions = UserLayerHandlerHelper.getPermissions();
        JSONHelper.putValue(userLayer, "permissions", permissions);
        //add warning if features were skipped
        if (ulayer.getFeatures_skipped() > 0) {
            JSONObject featuresSkipped = new JSONObject();
            JSONHelper.putValue(featuresSkipped, "featuresSkipped", ulayer.getFeatures_skipped());
            JSONHelper.putValue(userLayer, "warning", featuresSkipped);
        }
        ResponseHelper.writeResponse(params, userLayer);
    }

}
