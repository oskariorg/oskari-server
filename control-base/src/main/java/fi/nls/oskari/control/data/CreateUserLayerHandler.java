package fi.nls.oskari.control.data;
/**
 * Store zipped shape file set to oskari_user_store database
 *
 */
import com.vividsolutions.jts.geom.Geometry;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;


import fi.nls.oskari.map.userlayer.service.UserLayerDataService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;


import org.json.JSONObject;
import org.opengis.feature.type.FeatureType;

import org.opengis.referencing.operation.MathTransform;

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


@OskariActionRoute("CreateUserLayer")
public class CreateUserLayerHandler extends ActionHandler {

    private static final Logger log = LogFactory
            .getLogger(CreateUserLayerHandler.class);
    private final UserLayerDataService userlayerService = new UserLayerDataService();

    @Override
    public void init() {


    }


    @Override
    public void handleAction(ActionParameters params) throws ActionException {


        final HttpServletResponse response = params.getResponse();
        PrintWriter writer = null;

        try {
            writer = response.getWriter();
        } catch (IOException ex) {
            throw new ActionException("Couldn't get the zip shp file set",
                    ex);
        }


        try {

            Map fparams = new HashMap<String, String>();

            FileItem fileitem = getZipShpFiles(params, fparams);

            File file = unZip(fileitem);

            if (file == null) {
                return;
            }

            User user = params.getUser();

            FeatureJSON io = new FeatureJSON();

            ShapefileDataStore dataStore = new ShapefileDataStore(file.toURI().toURL());
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource source = dataStore.getFeatureSource(typeName);
            FeatureCollection collection = source.getFeatures();

            FeatureType schema = collection.getSchema();
            //TODO: Coordinate transformation support
            // Helsinki City crs is 3879
            // CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:3879");
            // TODO add geotools specs to prj file   epsg:3879 data schema.getCoordinateReferenceSystem();
            // CoordinateReferenceSystem target = CRS.decode("EPSG:3067");

            MathTransform transform = null;
            //TODO: if (sourceCrs != null) transform = CRS.findMathTransform(sourceCrs, target);


            JSONObject geoJson = JSONHelper.createJSONObject(io.toString(collection));

            // Store geojson via ibatis

            UserLayer ulayer = userlayerService.storeUserData(geoJson, user, typeName, fparams);

            params.getResponse().setContentType("application/json;charset=utf-8");
            ResponseHelper.writeResponse(params,  userlayerService.parseUserLayer2JSON(ulayer));

        } catch (Exception e) {
            throw new ActionException("Couldn't get the shp file set",
                    e);
        }

    }

    /**
     * unzip and write shp files as temp filesz
     *
     * @param params
     * @return fparams  formdata params
     * @return File of shp master file of shp file set  (.shp)
     */
    private FileItem getZipShpFiles(ActionParameters params, Map fparams) throws ActionException {


        InputStream is = null;
        FileOutputStream fos = null;
        FileItem shpFileItem = null;
        HttpServletRequest request = params.getRequest();


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
                        if(shpFileItem == null) shpFileItem = fileItem;

                    }
                    else
                    {
                        fparams.put(fileItem.getFieldName(), fileItem.getString());
                    }
                }
            } catch (Exception ex) {
                log.error("Could not parse request", ex);
            }
        }


        return shpFileItem;
    }

    /**
     *
     * @param zipFile
     * @return
     * @throws Exception
     */

    private static File unZip(FileItem zipFile) throws Exception {

        byte[] buffer = new byte[1024];

        String filename = null;

        ZipInputStream zis = new ZipInputStream(zipFile.getInputStream());
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {


            if (ze.isDirectory()) {
                ze = zis.getNextEntry();
                continue;
            }

            String fileName = ze.getName();

            String[] parts = fileName.split("[.]");

            if (parts.length < 2) return null;

            File newFile = File.createTempFile(parts[0],"."+ parts[1]);

            log.info("file unzip : " + newFile.getAbsoluteFile());

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();

            if (filename == null)
            {
                int i =  newFile.getPath().lastIndexOf(".");
                String[] parts3 =  {newFile.getPath().substring(0, i),newFile.getPath().substring(i)};

                filename = parts3[0];
            }
            else
            {
                // Use same file name basics


                int i =  newFile.getPath().lastIndexOf(".");
                String[] parts3 =  {newFile.getPath().substring(0, i),newFile.getPath().substring(i)};
                newFile.renameTo(new File(filename+parts3[1]));
            }
            newFile.deleteOnExit();
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        // master file of Esri shape file set
        return  new File(filename+".shp");
    }


}
