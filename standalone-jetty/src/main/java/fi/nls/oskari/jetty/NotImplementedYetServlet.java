package fi.nls.oskari.jetty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NotImplementedYetServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        httpServletResponse.getWriter().println("Not Implemented yet!");
    }
    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        httpServletResponse.getWriter().println("Not Implemented yet!");
    }
}
