package downloadbasket.data;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markokuo on 6.10.2015.
 */
public class NormalWayDownloads {
    private final Logger LOGGER = LogFactory.getLogger(NormalWayDownloads.class);
    private List<String> downloads = new ArrayList<>();

    /**
     * Add normal way download.
     * @param download
     */
    public void addDownload(String download){
        if(!downloads.contains(download)) {
            downloads.add(download);
        }
    }

    /**
     * Cheks at dowload is normal way download.
     * @param croppingMode the cropping mode
     * @param croppingLayer the cropping layer
     * @return
     */
    public boolean isNormalWayDownload(String croppingMode, String croppingLayer){
        boolean isNormalWay = false;

        for (String cropping : downloads) {
            if(cropping.contains("__")) {
                String tmp[] = cropping.split("__");
                if(tmp.length == 2){
                    if(tmp[0].equals(croppingMode) && tmp[1].equals(croppingLayer )) {
                        isNormalWay = true;
                        break;
                    }
                }
            } else {
                if(cropping.equals(croppingMode)) {
                    isNormalWay = true;
                    break;
                }
            }
        }

        LOGGER.debug("Checked normal way download. Cropping mode: "
                + croppingMode + ", cropping layer: "
                + croppingLayer + ". Is normal way download: " + isNormalWay + ".");

        return isNormalWay;
    }
}
