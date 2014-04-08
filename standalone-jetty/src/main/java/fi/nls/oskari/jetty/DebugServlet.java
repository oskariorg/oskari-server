package fi.nls.oskari.jetty;

import org.apache.commons.dbcp.BasicDataSource;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DebugServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setContentType("text/html;charset=utf-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.getWriter().println("<h1>DebugServlet in Oskari Standalone Jetty Server</h1>");

        try {
            BasicDataSource dataSource = lookupJNDIDataSource();
            httpServletResponse.getWriter().println("<h2>JNDI Data Source: " + dataSource.getUrl() + "</h2>");
        } catch (NamingException e) {
            httpServletResponse.getWriter().println("<h2>JNDI Data Source undefined.</h2>");
            e.printStackTrace();
        }
    }

    private BasicDataSource lookupJNDIDataSource() throws NamingException {
        InitialContext ic = new InitialContext();
        return (BasicDataSource)ic.lookup("java:comp/env/jdbc/OskariPool");
    }
}
