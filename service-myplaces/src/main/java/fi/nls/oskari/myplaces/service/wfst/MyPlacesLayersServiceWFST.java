package fi.nls.oskari.myplaces.service.wfst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.json.JSONException;
import org.oskari.wfst.CategoriesHelperWFST;
import org.oskari.wfst.InsertedFeature;
import org.oskari.wfst.MyPlaceCategoryHelper;
import org.oskari.wfst.TransactionResponse_110;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.myplaces.service.MyPlacesLayersService;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;

public class MyPlacesLayersServiceWFST extends BaseServiceWFST implements MyPlacesLayersService {

    @Override
    public Optional<MyPlaceCategory> getById(long id) throws ServiceException {
        List<MyPlaceCategory> categories = getByIds(new long[] { id });
        if (categories != null && categories.size() > 0) {
            return Optional.of(categories.get(0));
        }
        return Optional.empty();
    }

    @Override
    public List<MyPlaceCategory> getByIds(long[] ids) throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CategoriesHelperWFST.getCategoriesById(baos, ids);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            return MyPlaceCategoryHelper.parseFromGeoJSON(IOHelper.readString(conn), true);
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (JSONException e) {
            throw new ServiceException("Received invalid response from service", e);
        }
    }

    @Override
    public List<MyPlaceCategory> getByUserId(String uuid)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CategoriesHelperWFST.getCategoriesByUuid(baos, uuid);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            return MyPlaceCategoryHelper.parseFromGeoJSON(IOHelper.readString(conn), true);
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (JSONException e) {
            throw new ServiceException("Received invalid response from service", e);
        }
    }

    @Override
    public int insert(List<MyPlaceCategory> categories)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String[] handles = CategoriesHelperWFST.insertCategories(baos, categories);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            TransactionResponse_110 resp = readTransactionResp(conn);
            List<InsertedFeature> insertedFeatures = resp.getInsertedFeatures();
            setIds(categories, handles, insertedFeatures);
            return resp.getTotalInserted();
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    @Override
    public int update(List<MyPlaceCategory> categories)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CategoriesHelperWFST.updateCategories(baos, categories);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            return readTransactionResp(conn).getTotalUpdated();
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    @Override
    public int delete(long[] ids)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CategoriesHelperWFST.deleteCategories(baos, ids);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            return readTransactionResp(conn).getTotalDeleted();
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        }
    }

    private void setIds(List<MyPlaceCategory> categories, String[] handles,
            List<InsertedFeature> insertedFeatures) throws ServiceException {
        for (int i = 0; i < categories.size(); i++) {
            MyPlaceCategory category = categories.get(i);
            String handle = handles[i];
            InsertedFeature insertedFeature = insertedFeatures.stream()
                .filter(f -> handle.equals(f.getHandle()))
                .findAny()
                .orElseThrow(() -> new ServiceException(
                        "Could not find inserted feature with matching handle"));
            String prefixedId = insertedFeature.getFid();
            category.setId(CategoriesHelperWFST.removePrefixFromId(prefixedId));
        }
    }

}
