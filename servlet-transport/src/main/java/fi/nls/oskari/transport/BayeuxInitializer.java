package fi.nls.oskari.transport;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cometd.bayeux.server.BayeuxServer;

/**
 * Initializer for CometdServlet
 * 
 * @see javax.servlet.GenericServlet
 */
public class BayeuxInitializer extends GenericServlet
{
	private static final long serialVersionUID = -8679423990705602245L;

    /**
     * On servlet init creates BayeuxServer and TransportService 
     * 
     * @see TransportService
     * @see javax.servlet.GenericServlet#init()
     */
	public void init() throws ServletException
    {
        BayeuxServer bayeux = (BayeuxServer)getServletContext().getAttribute(BayeuxServer.ATTRIBUTE);
        new TransportService(bayeux);
    }

	/**
	 * Empty implementation of service (abstract method)
	 * 
	 * Throws ServletException
	 * 
	 * @see javax.servlet.GenericServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        throw new ServletException();
    }
    
    /** 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
    	super.destroy();
    }
}
