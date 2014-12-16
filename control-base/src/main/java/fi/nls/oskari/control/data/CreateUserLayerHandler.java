package fi.nls.oskari.control.data;
/**
 * Store zipped file set to oskari_user_store database
 *
 */

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
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
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

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


    @Override
    public void init() {


    }


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        // stop here if user isn't logged in
        params.requireLoggedInUser();

        final HttpServletResponse response = params.getResponse();
        final String target_epsg = params.getHttpParam(PARAM_EPSG_KEY, "EPSG:3067");

        try {

            // Only 1st file item is handled
            RawUpLoadItem loadItem = getZipFiles(params);

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
            params.getResponse().setContentType("text/plain;charset=utf-8");
            params.getResponse().setCharacterEncoding("UTF-8");

            JSONObject userLayer = userlayerService.parseUserLayer2JSON(ulayer);
            JSONObject permissions = OskariLayerWorker.getAllowedPermissions();
            JSONHelper.putValue(userLayer, "permissions", permissions);
            params.getResponse().getWriter().print(userLayer);

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
            e.printStackTrace();
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
                // diskFileItemFactory.setSizeThreshold(1024 * 1024 * 10); //10MB.


                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);

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

        byte[] buffer = new byte[1024];
        String imp_extension = null;
        String filename = null;

        ZipInputStream zis = new ZipInputStream(zipFile.getInputStream());
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {


            if (ze.isDirectory()) {
                ze = zis.getNextEntry();
                continue;
            }

            String fileName = ze.getName();

            int i = fileName.lastIndexOf(".");
            String[] parts = {fileName.substring(0, i), fileName.substring(i + 1)};
            // Clean and jump extra files
            String parts0 = checkFileName(parts[0]);
            if (parts0 == null) {
                ze = zis.getNextEntry();
                continue;
            }

            if (parts.length < 2) return null;
            if (ACCEPTED_FORMATS.contains(parts[1].toUpperCase())) {
                imp_extension = parts[1];
            }

            File newFile = File.createTempFile(parts0, "." + parts[1]);

            log.info("file unzip : " + newFile.getAbsoluteFile());

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();

            if (filename == null) {
                i = newFile.getPath().lastIndexOf(".");
                String[] parts3 = {newFile.getPath().substring(0, i), newFile.getPath().substring(i)};

                filename = parts3[0];
            } else {
                // Use same file name basics


                i = newFile.getPath().lastIndexOf(".");
                String[] parts3 = {newFile.getPath().substring(0, i), newFile.getPath().substring(i)};
               boolean success = newFile.renameTo(new File(filename + parts3[1]));
               if(!success) log.info("Rename failed in temp directory",newFile.getName(), " ->  ",filename + parts3[1] );
               else log.info("File renamed ",newFile.getName(), " ->  ",filename + parts3[1] );
            }
            newFile.deleteOnExit();
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        // is acceptable extension found
        if (imp_extension == null) {
            log.info("Acceptable format extension not found - valid extensions: ", ACCEPTED_FORMATS);
            return null;
        }

        // master file of import  file set
        return new File(filename + "." + imp_extension);
    }

    private static String checkFileName(String filenam) {
        String[] parts = filenam.split("\\/");
        // no dots any more allowed in filename
        if (parts[parts.length - 1].indexOf(".") > -1) return null;
        return parts[parts.length - 1];
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
