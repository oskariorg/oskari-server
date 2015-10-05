package fi.nls.oskari.control.data;
/**
 * Store zipped file set to oskari_user_store database
 *
 */

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;


import fi.nls.oskari.map.userlayer.domain.KMLGeoJsonCollection;
import fi.nls.oskari.map.userlayer.domain.GPXGeoJsonCollection;
import fi.nls.oskari.map.userlayer.domain.MIFGeoJsonCollection;
import fi.nls.oskari.map.userlayer.domain.SHPGeoJsonCollection;
import fi.nls.oskari.map.userlayer.service.GeoJsonWorker;
import fi.nls.oskari.map.userlayer.service.UserLayerDataService;
import fi.nls.oskari.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;


@OskariActionRoute("CreateUserLayer")
public class CreateUserLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(CreateUserLayerHandler.class);
    private final UserLayerDataService userlayerService = new UserLayerDataService();
    private static final List<String> ACCEPTED_FORMATS = Arrays.asList("SHP", "KML", "GPX", "MIF");
    private static final String IMPORT_SHP = ".SHP";
    private static final String IMPORT_GPX = ".GPX";
    private static final String IMPORT_MIF = ".MIF";
    private static final String IMPORT_KML = ".KML";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String USERLAYER_MAX_FILE_SIZE_MB = "userlayer.max.filesize.mb";
    final long userlayerMaxFileSizeMb = PropertyUtil.getOptional(USERLAYER_MAX_FILE_SIZE_MB, 10);
    private static final int MAX_FILES_IN_ZIP = 100;

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        // stop here if user isn't logged in
        params.requireLoggedInUser();

        final String target_epsg = params.getHttpParam(PARAM_EPSG_KEY, "EPSG:3067");

        try {

            // Only 1st file item is handled
            RawUpLoadItem loadItem = getZipFiles(params);

            // Checks file size

            File file = unZip(loadItem.getFileitem());

            if (file == null) {
                throw new ActionException("Couldn't find valid import file in zip file");
            }

            User user = params.getUser();
            // import format
            GeoJsonWorker geojsonWorker = null;

            if (file.getName().toUpperCase().indexOf(IMPORT_SHP) > -1) {
                geojsonWorker = new SHPGeoJsonCollection();
            } else if (file.getName().toUpperCase().indexOf(IMPORT_KML) > -1) {
                geojsonWorker = new KMLGeoJsonCollection();
            } else if (file.getName().toUpperCase().indexOf(IMPORT_GPX) > -1) {
                geojsonWorker = new GPXGeoJsonCollection();
            } else if (file.getName().toUpperCase().indexOf(IMPORT_MIF) > -1) {
                geojsonWorker = new MIFGeoJsonCollection();
            }
            // Parse import data to geojson
            if (!geojsonWorker.parseGeoJSON(file, target_epsg)) {
                throw new ActionException("Couldn't parse geoJSON out of import file");
            }


            // Store geojson via ibatis
            UserLayer ulayer = userlayerService.storeUserData(geojsonWorker, user, loadItem.getFparams());

            // workaround because of IE iframe submit json download functionality
            //params.getResponse().setContentType("application/json;charset=utf-8");
            //ResponseHelper.writeResponse(params, userlayerService.parseUserLayer2JSON(ulayer));
            final HttpServletResponse response = params.getResponse();
            response.setContentType("text/plain;charset=utf-8");
            response.setCharacterEncoding("UTF-8");

            JSONObject userLayer = userlayerService.parseUserLayer2JSON(ulayer);
            JSONObject permissions = OskariLayerWorker.getAllowedPermissions();
            JSONHelper.putValue(userLayer, "permissions", permissions);
            response.getWriter().print(userLayer);

        } catch (Exception e) {
            throw new ActionException("Couldn't get the import file set",
                    e);
        }

    }

    /**
     * unzip and write shp files as temp filesz
     *
     * @param params
     * @return File of import master file of  file set  (.shp, .kml, mif)
     */
    private RawUpLoadItem getZipFiles(ActionParameters params) throws ActionException {


        InputStream is = null;
        FileOutputStream fos = null;
        FileItem impFileItem = null;
        HttpServletRequest request = params.getRequest();

        try {
            // Incoming strings are in UTF-8 but they're not read as such unless we force it...
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Couldnt setup UTF-8 encoding");
        }

        Map fparams = new HashMap<String, String>();
        RawUpLoadItem loadItem = new RawUpLoadItem();

        try {
            request.setCharacterEncoding("UTF-8");

            if (request.getContentType().indexOf("multipart") > -1) {

                DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();

                /*
                * Set the file size limit in bytes. This should be set as an
                * initialization parameter
                */
                // diskFileItemFactory.setSizeThreshold(1024 * 1024 * 10);

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);

                // Set size limit
                upload.setSizeMax(userlayerMaxFileSizeMb * 1024 * 1024);

                List items = null;

                try {
                    items = upload.parseRequest(request);
                } catch (FileUploadException ex) {
                    log.error("Could not parse request", ex);
                }
                try {
                    ListIterator li = items.listIterator();

                    while (li.hasNext()) {
                        FileItem fileItem = (FileItem) li.next();
                        if (!fileItem.isFormField()) {
                            // Take only 1st one
                            if (impFileItem == null) impFileItem = fileItem;

                        } else {
                            fparams.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
                        }
                    }
                } catch (Exception ex) {
                    log.error("Could not parse request", ex);
                }
            }
        } catch (Exception ex) {
            log.error("Could not parse request", ex);
        }
        loadItem.setFileitem(impFileItem);
        loadItem.setFparams(fparams);

        return loadItem;
    }

    /**
     * @param zipFile
     * @return
     * @throws Exception
     */

    private static File unZip(FileItem zipFile) throws Exception {

        FileHelper mainFile = null;
        ZipInputStream zis = null;

        try {
            zis = new ZipInputStream(zipFile.getInputStream());
            ZipEntry ze = zis.getNextEntry();
            String filesBaseName = null;
            int fileCount = 0;
            while (ze != null) {
                fileCount++;
                if(fileCount > MAX_FILES_IN_ZIP) {
                    // safeguard against infinite loop, userlayers shouldn't have this many files in any case
                    break;
                }

                if (ze.isDirectory()) {
                    zis.closeEntry();
                    ze = zis.getNextEntry();
                    continue;
                }
                FileHelper file = handleZipEntry(ze, zis, filesBaseName);
                if(file == null) {
                    zis.closeEntry();
                    ze = zis.getNextEntry();
                    continue;
                }

                // use the same base name for all files in zip
                int i = file.getSavedTo().lastIndexOf(".");
                filesBaseName = file.getSavedTo().substring(0, i);

                if (mainFile == null && file.isOfType(ACCEPTED_FORMATS)) {
                    mainFile = file;
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
        } finally {
            IOHelper.close(zis);
        }

        if(mainFile == null) {
            return null;
        }
        return mainFile.getFile();
    }

    private static FileHelper handleZipEntry(ZipEntry ze, ZipInputStream zis, String tempFileBaseName) {

        FileHelper file = new FileHelper(ze.getName());
        if (!file.hasNameAndExtension()) {
            return null;
        }

        // TODO: File.createTempFile() says "The prefix string must be at least three characters long" maybe check this?
        File newFile = null;
        FileOutputStream fos = null;
        try {
            newFile = File.createTempFile(file.getSafeName(), "." + file.getExtension());
            log.debug("file unzip : " + newFile.getAbsoluteFile());
            fos = new FileOutputStream(newFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException ex) {
            log.warn(ex, "Error unzipping file:", file);
        } finally {
            if(newFile != null) {
                file.setSavedTo(newFile.getPath());
                newFile.deleteOnExit();
            }
            IOHelper.close(fos);
        }

        if(tempFileBaseName != null) {
            // rename tempfile if we have an existing basename (shapefiles need to have same basename)
            final String newName = tempFileBaseName + "." + file.getExtension();
            File renameLocation = new File(newName);
            boolean success = newFile.renameTo(renameLocation);
            if(!success) {
                log.warn("Rename failed in temp directory", newFile.getName(), " ->  ",newName );
            } else {
                // update path
                file.setSavedTo(renameLocation.getPath());
                log.debug("File renamed ",newFile.getName(), " ->  ",newName );
            }
        }
        return file;
    }

    class RawUpLoadItem {
        FileItem fileitem;
        Map<String, String> fparams;
        String imp_extension;

        FileItem getFileitem() {
            return fileitem;
        }

        void setFileitem(FileItem fileitem) {
            this.fileitem = fileitem;
        }

        Map<String, String> getFparams() {
            return fparams;
        }

        void setFparams(Map<String, String> fparams) {
            this.fparams = fparams;
        }

        String getImp_extension() {
            return imp_extension;
        }

        void setImp_extension(String imp_extension) {
            this.imp_extension = imp_extension;
        }
    }
}
