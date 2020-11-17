package org.oskari.announcements.helpers;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.announcements.helpers.AnnouncementParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.time.*;

/**
 * Helper for seutumasia db handling.
 */
public class AnnouncementsDBHelper {
    private static final String PROPERTY_DB_URL = "db.url";
    private static final String PROPERTY_DB_USER = "db.username";
    private static final String PROPERTY_DB_PASSWORD = "db.password";

    private static final Logger LOG = LogFactory.getLogger(AnnouncementsDBHelper.class);

    /**
     * Gets DB connection.
     * @return
     * @throws SQLException
     */
    private static final Connection getConnection() throws SQLException {
        String url = PropertyUtil.get(PROPERTY_DB_URL);
        String user = PropertyUtil.get(PROPERTY_DB_USER);
        String password = PropertyUtil.get(PROPERTY_DB_PASSWORD);
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection;
    }

    /**
     * Handle getting announcements from database
     * @param params
     * @return
     * @throws JSONException
     * @throws ActionParamsException
     */
    public static JSONObject getAnnouncements() throws JSONException, ActionParamsException {
        Connection conn = null;

        JSONArray results = new JSONArray();
        String sql = "";
        String sqlWithParams = "";

        try  {
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT id, title, content, begin_date, end_date, active ");
            sb.append("FROM oskari_announcements ");
            sb.append("WHERE begin_date <= ? ::DATE AND end_date >= ? ::DATE ");
            sb.append("ORDER BY id DESC;");

            sql = sb.toString();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            //Set current date to where clause so we don't get outdated announcements
            Date date = Date.valueOf(LocalDate.now());
            pstmt.setDate(1, date);
            pstmt.setDate(2, date);

            sqlWithParams = pstmt.toString();
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("id", rs.getInt("id"));
                row.put("title", rs.getString("title"));
                row.put("content", rs.getString("content"));
                row.put("begin_date", df.format(rs.getDate("begin_date")));
                row.put("end_date", df.format(rs.getDate("end_date")));
                row.put("active", rs.getBoolean("active"));
                results.put(row);
            }

        } catch (SQLException e) {
            LOG.error(e, "Cannot create SQL query, sql=" + sqlWithParams);

        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("data", results);

        return json;

    }

    /**
     * Handle getting admin-announcements from database
     * @param params
     * @return
     * @throws JSONException
     * @throws ActionParamsException
     */
    public static JSONObject getAdminAnnouncements() throws JSONException, ActionParamsException {
        Connection conn = null;

        JSONArray results = new JSONArray();
        String sql = "";
        String sqlWithParams = "";

        try  {
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT id, title, content, begin_date, end_date, active ");
            sb.append("FROM oskari_announcements ");
            sb.append("ORDER BY id DESC;");

            sql = sb.toString();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            sqlWithParams = pstmt.toString();
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("id", rs.getInt("id"));
                row.put("title", rs.getString("title"));
                row.put("content", rs.getString("content"));
                row.put("begin_date", df.format(rs.getDate("begin_date")));
                row.put("end_date", df.format(rs.getDate("end_date")));
                row.put("active", rs.getBoolean("active"));
                results.put(row);
            }

        } catch (SQLException e) {
            LOG.error(e, "Cannot create SQL query, sql=" + sqlWithParams);

        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("data", results);

        return json;

    }

    /**
     * Handle saving announcement to database
     * @param params
     * @return
     * @throws JSONException
     * @throws ActionParamsException
     */
    public static JSONObject saveAnnouncement(ActionParameters params) throws JSONException, ActionParamsException {
        Connection conn = null;

        JSONArray results = new JSONArray();
        String sql = "";
        String sqlWithParams = "";

        try  {
        
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO oskari_announcements(title, content, begin_date, end_date, active) ");
            sb.append("VALUES (? ::VARCHAR, ? ::VARCHAR, ? ::DATE, ? ::DATE, ? ::BOOLEAN) ");
            sb.append("RETURNING id;");

            

            sql = sb.toString();
            List<AnnouncementParams> announcementParams = AnnouncementsParser.parseAnnouncement(params);

            PreparedStatement pstmt = conn.prepareStatement(sql);

            int index = 1;
            for (int i=0; i<announcementParams.size(); i++) {
                AnnouncementParams announcementParam = announcementParams.get(i);
                if (announcementParam.getValue() instanceof Integer) {
                    pstmt.setInt(index, (int)announcementParam.getValue());
                    index++;
                } else if (announcementParam.getValue() instanceof String) {
                    pstmt.setString(index, (String)announcementParam.getValue());
                    index++;
                } else if (announcementParam.getValue() instanceof Date) {
                    pstmt.setDate(index, (Date)announcementParam.getValue());
                    index++;
                } else if (announcementParam.getValue() instanceof Boolean) {
                    pstmt.setBoolean(index, (Boolean)announcementParam.getValue());
                    index++;
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.put(rs.getInt("id"));
            }

        } catch (SQLException e) {
            LOG.error(e, "Cannot create SQL query, sql=" + sqlWithParams);

        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("data", results);

        return json;

    }

    /**
     * Handle saving announcement to database
     * @param params
     * @return
     * @throws JSONException
     * @throws ActionParamsException
     */
    public static JSONObject updateAnnouncement(ActionParameters params) throws JSONException, ActionParamsException {
        Connection conn = null;

        JSONArray results = new JSONArray();
        String sql = "";
        String sqlWithParams = "";

        try  {
        
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE oskari_announcements ");
            sb.append("SET title = ? :: VARCHAR, content = ? :: VARCHAR, begin_date = ? :: DATE, end_date = ? :: DATE, active = ? :: BOOLEAN ");
            sb.append("WHERE id = ? ::INTEGER ");
            sb.append("RETURNING id;");

            

            sql = sb.toString();
            List<AnnouncementParams> announcementParams = AnnouncementsParser.parseAnnouncement(params);

            PreparedStatement pstmt = conn.prepareStatement(sql);

            int index = 1;
            for (int i=0; i<announcementParams.size(); i++) {
                AnnouncementParams announcementParam = announcementParams.get(i);
                if (announcementParam.getValue() instanceof Integer) {
                    pstmt.setInt(6, (int)announcementParam.getValue());
                } else if (announcementParam.getValue() instanceof String) {
                    pstmt.setString(index, (String)announcementParam.getValue());
                    index++;
                } else if (announcementParam.getValue() instanceof Date) {
                    pstmt.setDate(index, (Date)announcementParam.getValue());
                    index++;
                } else if (announcementParam.getValue() instanceof Boolean) {
                    pstmt.setBoolean(index, (Boolean)announcementParam.getValue());
                    index++;
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.put(rs.getInt("id"));
            }

        } catch (SQLException e) {
            LOG.error(e, "Cannot create SQL query, sql=" + sqlWithParams);

        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }

        JSONObject json = new JSONObject();
        json.put("data", results);

        return json;

    }

    /**
     * Handle deleting announcements from database
     * @param params
     * @return
     * @throws JSONException
     * @throws ActionParamsException
     */
    public static JSONObject deleteAnnouncement(ActionParameters params) throws JSONException, ActionParamsException {
        Connection conn = null;

        JSONArray results = new JSONArray();
        String sql = "";
        String sqlWithParams = "";

        try  {
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM oskari_announcements ");
            sb.append("WHERE id = ? ");
            sb.append("RETURNING id;");
            sql = sb.toString();

            List<AnnouncementParams> announcementParams = AnnouncementsParser.parseAnnouncement(params);

            PreparedStatement pstmt = conn.prepareStatement(sql);

            int index = 1;
            for (int i=0; i<announcementParams.size(); i++) {
                AnnouncementParams announcementParam = announcementParams.get(i);
                if (announcementParam.getValue() instanceof Integer) {
                    pstmt.setInt(index, (int)announcementParam.getValue());
                    index++;
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.put(rs.getInt("id"));
            }

        } catch (SQLException e) {
            LOG.error(e, "Cannot create SQL query, sql=" + sqlWithParams);

        } finally {
            if(conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }
        JSONObject json = new JSONObject();
        json.put("data", results);

        return json;
    }


}
