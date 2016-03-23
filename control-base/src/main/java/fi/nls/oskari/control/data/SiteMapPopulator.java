package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by RLINKALA on 10.2.2016.
 */
@OskariActionRoute("SiteMapPopulator")
public class SiteMapPopulator extends ActionHandler {

    private final static Logger log = LogFactory.getLogger(GetGeoLocatorSearchResultHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        log.debug("creating the sitemap");

        try{

            createSiteMapFile("Some Content");


        }catch(Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }

    }

    private void createSiteMapFile(String fileContent) throws IOException{

        //String content = "This is the content to write into file";

        File file = new File("C:\\Omat\\temppi\\filename.txt");

        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        bw.write(fileContent);
        bw.write("</urlset>");
        bw.close();

        System.out.println("Done");

    }
}
