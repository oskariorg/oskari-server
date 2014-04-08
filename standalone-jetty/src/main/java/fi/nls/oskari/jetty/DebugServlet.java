package fi.nls.oskari.jetty;

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
            Integer entry = lookupJNDIEnvironmentEntry();
            httpServletResponse.getWriter().println("<h2>JNDI Environment Entry: " + Integer.toString(entry) + "</h2>");
        } catch (NamingException e) {
            httpServletResponse.getWriter().println("<h2>JNDI Environment Entry undefined.</h2>");
            e.printStackTrace();
        }
    }

    private Integer lookupJNDIEnvironmentEntry() throws NamingException {
        InitialContext ic = new InitialContext();
        return (Integer)ic.lookup("java:comp/env/jdbc/OskariPool");
    }
}
