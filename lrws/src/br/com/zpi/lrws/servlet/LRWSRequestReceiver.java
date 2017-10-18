package br.com.zpi.lrws.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import br.com.zpi.lrws.LRWSException;

public class LRWSRequestReceiver extends LRWSServlet {
	private static final long serialVersionUID = 1L;
	private LRWSEndpointMap epmap = null;

	/*
	 * #########################################################################
	 * ##### DELETE
	 * #########################################################################
	 * #####
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doDelete(req, resp);
		
		String smap = getServletContext().getAttribute("LRWSServletMapping").toString();
		if (smap != null) {
			try {
				epmap = (LRWSEndpointMap) Class.forName(smap).newInstance();
			} catch (Exception e) {

			}

		}

		// Get map
		if (epmap == null) {
			out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Servlet Mapping"), true));
			return;
		}
		// MAP
		String serv = epmap.getServletReceiver(address);
		if (serv != null) {
			try {
				LRWSServlet servlet = (LRWSServlet) Class.forName(serv).newInstance();
				if (servlet != null)
					servlet.doDelete(req, resp);
			} catch (Exception e) {

			}
		}

		out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Not Found"), true));
		return;
	}

	/*
	 * #########################################################################
	 * ##### GET
	 * #########################################################################
	 * #####
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);

		String smap = getServletContext().getAttribute("LRWSServletMapping").toString();
		if (smap != null) {
			try {
				epmap = (LRWSEndpointMap) Class.forName(smap).newInstance();
			} catch (Exception e) {

			}

		}
		
		// Get map
		if (epmap == null) {
			out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Servlet Mapping"), true));
			return;
		}
		// MAP
		String serv = epmap.getServletReceiver(address);
		if (serv != null) {
			try {
				LRWSServlet servlet = (LRWSServlet) Class.forName(serv).newInstance();
				if (servlet != null)
					servlet.doGet(req, resp);
			} catch (Exception e) {

			}
		}

		out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Not Found"), true));
		return;
	}

	/*
	 * #########################################################################
	 * ##### POST
	 * #########################################################################
	 * #####
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
		
		String smap = getServletContext().getAttribute("LRWSServletMapping").toString();
		if (smap != null) {
			try {
				epmap = (LRWSEndpointMap) Class.forName(smap).newInstance();
			} catch (Exception e) {

			}

		}
		// Get map
		if (epmap == null) {
			out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Servlet Mapping"), true));
			return;
		}
		// MAP
		String serv = epmap.getServletReceiver(address);
		if (serv != null) {
			try {
				LRWSServlet servlet = (LRWSServlet) Class.forName(serv).newInstance();
				if (servlet != null)
					servlet.doPost(req, resp);

			} catch (Exception e) {

			}
		}

		out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Not Found"), true));
		return;
	}

	/*
	 * #########################################################################
	 * ##### PUT
	 * #########################################################################
	 * #####
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
		
		String smap = getServletContext().getAttribute("LRWSServletMapping").toString();
		if (smap != null) {
			try {
				epmap = (LRWSEndpointMap) Class.forName(smap).newInstance();
			} catch (Exception e) {

			}

		}
		
		// Get map
		if (epmap == null) {
			out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Servlet Mapping"), true));
			return;
		}
		// MAP
		String serv = epmap.getServletReceiver(address);
		if (serv != null) {
			try {
				LRWSServlet servlet = (LRWSServlet) Class.forName(serv).newInstance();
				if (servlet != null)
					servlet.doPut(req, resp);

			} catch (Exception e) {

			}
		}

		out.println(lrws.lin2json(new LRWSException("E", "SERVLET", "Not Found"), true));
		return;
	}

}
