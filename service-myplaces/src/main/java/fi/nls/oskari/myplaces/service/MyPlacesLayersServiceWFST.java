package fi.nls.oskari.myplaces.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.wfst.CategoriesHelperWFST;

import fi.nls.oskari.domain.map.MyPlaceCategory;
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
            return parseFromGeoJSON(conn);
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
            return parseFromGeoJSON(conn);
        } catch (XMLStreamException e) {
            throw new ServiceException("Failed to create WFS-T request", e);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (JSONException e) {
            throw new ServiceException("Received invalid response from service", e);
        }
    }

    @Override
    public long[] insert(List<MyPlaceCategory> categories)
            throws ServiceException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CategoriesHelperWFST.insertCategories(baos, categories);
            HttpURLConnection conn = getConnection();
            IOHelper.post(conn, APPLICATION_XML, baos);
            String[] ids = readTransactionResp(conn).getInsertedIds();
            return CategoriesHelperWFST.removePrefixFromIds(ids);
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

    private List<MyPlaceCategory> parseFromGeoJSON(HttpURLConnection conn)
            throws IOException, JSONException {
        String resp = IOHelper.readString(conn);
        JSONObject featureCollection = new JSONObject(resp);
        JSONArray featuresArray = featureCollection.getJSONArray("features");
        final int n = featuresArray.length();
        List<MyPlaceCategory> categories = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            JSONObject myPlaceCategoryJSON = featuresArray.getJSONObject(i);
            JSONObject properties = myPlaceCategoryJSON.getJSONObject("properties");
            MyPlaceCategory category = parseFromGeoJSON(properties);
            category.setId(myPlaceCategoryJSON.getLong("id"));
            categories.add(category);
        }
        return categories;
    }

    private MyPlaceCategory parseFromGeoJSON(JSONObject properties)
            throws JSONException {
        MyPlaceCategory category = new MyPlaceCategory();

        category.setUuid(properties.getString("uuid"));
        category.setPublisher_name(properties.getString("publisher_name"));

        category.setCategory_name(properties.getString("category_name"));
        category.setDefault(properties.getBoolean("default"));

        category.setStroke_width(properties.getInt("stroke_width"));
        category.setStroke_color(properties.getString("stroke_color"));
        category.setStroke_linejoin(properties.getString("stroke_linejoin"));
        category.setStroke_linecap(properties.getString("stroke_linecap"));
        category.setStroke_dasharray(properties.getString("stroke_dasharray"));

        category.setFill_color(properties.getString("fill_color"));
        category.setFill_pattern(properties.getInt("fill_pattern"));

        category.setDot_color(properties.getString("dot_color"));
        category.setDot_size(properties.getInt("dot_size"));
        category.setDot_shape(properties.getString("dot_shape"));

        category.setBorder_width(properties.getInt("border_width"));
        category.setBorder_color(properties.getString("border_color"));
        category.setBorder_linejoin(properties.getString("border_linejoin"));
        category.setBorder_dasharray(properties.getString("border_dasharray"));

        return category;
    }
}
