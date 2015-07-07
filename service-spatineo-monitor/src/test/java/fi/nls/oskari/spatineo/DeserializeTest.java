package fi.nls.oskari.spatineo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import fi.nls.oskari.spatineo.dao.OskariDao;
import fi.nls.oskari.spatineo.dao.SpatineoServalDao;
import fi.nls.oskari.spatineo.dao.WmsServiceStatusDao;
import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.PorttiBackendStatusDto;
import fi.nls.oskari.spatineo.dto.SpatineoResponseDto;
import org.junit.Ignore;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class DeserializeTest {

    @Test
    public void multipleStatusTest() throws Exception {
        final InputStream is = DeserializeTest.class
                .getResourceAsStream("/multiple_response_partial_error.json");
        try {
            final SpatineoResponseDto response = new ObjectMapper()
                    .readValue(is, SpatineoResponseDto.class);
            assertEquals("OK", response.status);
            assertEquals("ERROR", response.result.get(0).status);
            assertEquals("OK", response.result.get(1).status);
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    @Test @Ignore("Performs database queries.")
    public void queryTest() throws Exception {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("oskaridb");

        final OskariDao dao = new OskariDao(dataSource);

        System.err.println(dao.findWmsMapLayerData());
    }

    @Test @Ignore("Performs external HTTP requests.")
    public void requestTest() throws Exception {
        final SpatineoServalDao dao = new SpatineoServalDao("https://monitor.spatineo.com/api/public/availability-1.0");
        final OskariMapLayerDto gtk = new OskariMapLayerDto(1L, Calendar.getInstance(), "0", "http://geomaps2.gtk.fi/ArcGIS/services/GTKWMS/MapServer/WMSServer"),
                tripla = new OskariMapLayerDto(2L, Calendar.getInstance(), "linkkimakkarat", "http://lada.infotripla.fi/geoserver/tilannekuva/wms"),
                metla = new OskariMapLayerDto(3L, Calendar.getInstance(), "Metla:muulptukki_0610", "http://kartta.metla.fi/geoserver/ows?");
        System.err.println(dao.checkServiceStatus(ImmutableList.of(gtk, tripla, metla)));
        System.err.println(dao.checkServiceStatus(ImmutableList.of(tripla, metla)));
        System.err.println(dao.checkServiceStatus(ImmutableList.of(gtk, tripla)));
    }

    public static final String SERVAL_URL = "https://monitor.spatineo.com/api/public/availability-1.0";

    private static int CHUNK_SIZE = 8;

    @Test @Ignore("Performs external HTTP requests.")
    public void mainTest() throws Exception {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("common.lportal");

        final OskariDao oskariDao = new OskariDao(dataSource);
        final WmsServiceStatusDao serviceStatusDao = new WmsServiceStatusDao(dataSource);
        final SpatineoServalDao spatineoDao = new SpatineoServalDao(SERVAL_URL);
        for (final List<OskariMapLayerDto> layers : Lists.partition(oskariDao.findWmsMapLayerData(), CHUNK_SIZE)) {
            System.err.printf("layers: %s%n", layers);
            final SpatineoResponseDto spatineoResponse = spatineoDao.checkServiceStatus(layers);
            System.err.printf("results: %s%n", spatineoResponse.result);
            for (int i = 0; i < layers.size(); i++) {
                final OskariMapLayerDto l = layers.get(i);
                final SpatineoResponseDto.Result s = spatineoResponse.result.get(i);
                serviceStatusDao.insertStatus(new PorttiBackendStatusDto(l.id, s.status, s.statusMessage, s.infoUrl));
                System.err.printf("inserting: <%s> <%s> <%s> <%s>%n", l.id, s.status, s.statusMessage, s.infoUrl);
            }
        }
    }

}
