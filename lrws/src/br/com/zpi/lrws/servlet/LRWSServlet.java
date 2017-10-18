package br.com.zpi.lrws.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import br.com.zpi.lrws.LRWS;

public class LRWSServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private LRWSBodyRequest bodyreq = null;
	public LRWS lrws = null;
	public JSONObject jsonBody = null;
	public JSONArray jsonBodyA = null;
	public JSONObject jsonPars = null;
	public String address = null;
	public PrintWriter out = null;
	
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initialize(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initialize(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initialize(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		initialize(req, resp);
	}

	/*
	 * #########################################################################
	 * ##### INITIALIZE PARAMS
	 * #########################################################################
	 * #####
	 */
	private void initialize(HttpServletRequest req, HttpServletResponse resp) {
		clear();

		String encoding = getServletContext().getAttribute("encoding").toString();
		lrws = new LRWS(encoding);

		try {
			out = resp.getWriter();
		} catch (Exception e) {

		}

		bodyreq = new LRWSBodyRequest(req, encoding);

		if (bodyreq != null) {
			jsonPars = bodyreq.getParametersJSON();
			jsonBody = bodyreq.getBodyJSON();
			jsonBodyA = bodyreq.getBodyJSONA();
		}

		if (address.startsWith(req.getContextPath())) {
			address = address.substring(req.getContextPath().length());
		}

	}

	/*
	 * #########################################################################
	 * ##### CLEAR PARAMS
	 * #########################################################################
	 * #####
	 */

	private void clear() {
		bodyreq = null;
		lrws = null;
		out = null;
		jsonBody = null;
		jsonBodyA = null;
		jsonPars = null;
		address = null;
	}
	
}
