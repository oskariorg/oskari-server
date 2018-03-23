package org.oskari.control.userlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

/**
 * Store zipped file set to oskari_user_store database
 */
@OskariActionRoute("CreateUserLayer")
public class CreateUserLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(CreateUserLayerHandler.class);
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_SOURCE_EPSG_KEY = "sourceEpsg";
    private static final String USERLAYER_MAX_FILE_SIZE_MB = "userlayer.max.filesize.mb";
    private static final int MAX_FILES_IN_ZIP = 100;
    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * 1024;

    private final int userlayerMaxFileSize = PropertyUtil.getOptional(USERLAYER_MAX_FILE_SIZE_MB, 10) * 1024 * 1024;
    private final UserLayerDataService userlayerService = new UserLayerDataService();
    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        // stop here if user isn't logged in
        params.requireLoggedInUser();

        final CoordinateReferenceSystem sourceCRS;
        final CoordinateReferenceSystem targetCRS;
        try {
            String source_epsg = params.getHttpParam(PARAM_SOURCE_EPSG_KEY);
            String target_epsg = params.getHttpParam(PARAM_EPSG_KEY, "EPSG:3067");
            sourceCRS = source_epsg != null ? CRS.decode(source_epsg) : null;
            targetCRS = CRS.decode(target_epsg);
        } catch (Exception e) {
            throw new ActionParamsException("Failed to decode CoordinateReferenceSystem from either "
                    + PARAM_EPSG_KEY + " or from " + PARAM_EPSG_KEY);
        }

        List<FileItem> fileItems = getFileItems(params.getRequest());

        Map<String, String> formParams = fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
        log.debug("Parsed form parameters:", formParams);

        List<File> unzipped = null;
        SimpleFeatureCollection fc;
        try {
            FileItem zipFile = fileItems.stream()
                    .filter(f -> !f.isFormField())
                    .findAny() // If there are more files we'll get the zip or fail miserably
                    .orElseThrow(() -> new ActionParamsException("No file entries"));
            log.debug("Using value from field:", zipFile.getFieldName(), "as the zip file");

            unzipped = unZip(zipFile);
            File mainFile = getMainFile(unzipped);
            if (mainFile == null) {
                throw new ActionParamsException("Couldn't find valid file for import in zip file");
            }
            FeatureCollectionParser parser = getParser(mainFile);
            fc = parse(parser, mainFile, sourceCRS, targetCRS);
        } finally {
            for (FileItem fileItem : fileItems) {
                fileItem.delete();
            }
            if (unzipped != null) {
                for (File file : unzipped) {
                    file.delete();
                }
            }
        }

        try {
            UserLayer ulayer = userlayerService.storeUserData(fc, params.getUser(), formParams);
            writeResponse(params, ulayer);
        } catch (ServiceException e) {
            throw new ActionException("unable_to_store_data", e);
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

    private List<File> unZip(FileItem zipFile) throws ActionException {
        try (InputStream in = zipFile.getInputStream()) {
            return unZip(in);
        } catch (IOException e) {
            throw new ActionException("Failed to unzip file", e);
        }
    }

    private List<File> unZip(InputStream in) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(in)) {
            List<File> files = new ArrayList<>();
            int filesInZip = 0;
            // Name all the files we find from zip with the same pattern
            // baseName.{ext} where {ext} is taken from the original filename
            // Use a 24 letter random UUID as baseName
            String baseName = UUID.randomUUID().toString().substring(0, 24);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                filesInZip++;
                if (filesInZip > MAX_FILES_IN_ZIP) {
                    // safeguard against infinite loop, userlayers shouldn't have this many files in any case
                    break;
                }
                if (ze.isDirectory()) {
                    continue;
                }
                String ext = getFileExt(ze.getName());
                if (ext == null) {
                    continue;
                }
                String name = baseName + '.' + ext;
                File file = writeToTempFile(zis, name);
                files.add(file);
            }
            return files;
        }
    }

    private String getFileExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i > name.length() - 1) {
            return null;
        }
        return name.substring(i + 1);
    }

    private File writeToTempFile(InputStream in, String name) throws IOException {
        File file = new File(getTempDir(), name);
        try (OutputStream out = new FileOutputStream(file)) {
            IOHelper.copy(in, out);
        }
        return file;
    }

    private File getTempDir() throws IOException {
        File tmp = File.createTempFile("temp", null);
        File dir = tmp.getParentFile();
        tmp.delete();
        return dir;
    }

    private File getMainFile(List<File> files) {
        for (File file : files) {
            String ext = getFileExt(file.getName());
            if (FeatureCollectionParsers.hasByFileExt(ext)) {
                return file;
            }
        }
        return null;
    }

    private FeatureCollectionParser getParser(File mainFile) {
        String ext = getFileExt(mainFile.getName());
        return FeatureCollectionParsers.getByFileExt(ext);
    }

    private SimpleFeatureCollection parse(FeatureCollectionParser parser, File file,
            CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS)
                    throws ActionException {
        try {
            return parser.parse(file, sourceCRS, targetCRS);
        } catch (ServiceException e) {
            throw new ActionException("Failed to parse Feature Collection", e);
        }
    }

    private void writeResponse(ActionParameters params, UserLayer ulayer) throws ActionException {
        try {
            // workaround because of IE iframe submit json download functionality
            //params.getResponse().setContentType("application/json;charset=utf-8");
            //ResponseHelper.writeResponse(params, userlayerService.parseUserLayer2JSON(ulayer));
            final HttpServletResponse response = params.getResponse();
            response.setContentType("text/plain;charset=utf-8");
            response.setCharacterEncoding("UTF-8");

            JSONObject userLayer = userlayerService.parseUserLayer2JSON(ulayer);
            JSONHelper.putValue(userLayer, "featuresCount", ulayer.getFeatures_count());
            JSONObject permissions = OskariLayerWorker.getAllowedPermissions();
            JSONHelper.putValue(userLayer, "permissions", permissions);
            //add warning if features were skipped
            if (ulayer.getFeatures_skipped() > 0) {
                JSONObject featuresSkipped = new JSONObject();
                JSONHelper.putValue(featuresSkipped, "featuresSkipped", ulayer.getFeatures_skipped());
                JSONHelper.putValue(userLayer, "warning", featuresSkipped);
            }
            response.getWriter().print(userLayer);
        } catch (IOException e) {
            throw new ActionException("Failed to write response", e);
        }
    }

}
