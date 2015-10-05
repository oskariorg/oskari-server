package fi.nls.oskari.printout.servlet.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RequestUTF8Filter implements javax.servlet.Filter {

	
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	
	public void doFilter(ServletRequest req, ServletResponse rsp,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		request.setCharacterEncoding("UTF-8");

		chain.doFilter(req, rsp);

	}

	
	public void destroy() {

	}

}
