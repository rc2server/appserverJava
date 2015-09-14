package edu.wvu.stat.rc2;

import java.io.IOException;

import java.math.BigInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Response;
import org.skife.jdbi.v2.DBI;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCLoginToken;
import edu.wvu.stat.rc2.persistence.RCLoginTokenQueries;
import edu.wvu.stat.rc2.persistence.RCUser;
import static edu.wvu.stat.rc2.Rc2AppConfiguration.*;

public class Rc2AuthServletFilter implements Filter {

	DBI _dbi;
	
	public Rc2AuthServletFilter(PGDataSourceFactory factory) {
			_dbi = factory.createDBI();
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException 
	{
		HttpServletRequest req = (HttpServletRequest)request;
		//always allow login requests via POST
		String path = req.getPathInfo();
		if (path != null && path.equals("login") && req.getMethod().equalsIgnoreCase("POST"))
			return;
		
		//for robots, return a basic string saying everything is disallowed
		if (path.equals("/robots.txt")) {
			abortRequest(response, Response.SC_OK, "User-agent: *\nDisallow: /\n");
			return;
		}

		//check for auth header first, then websocket protocol
		String tokenString = req.getHeader("RC2-Auth");
		if (null == tokenString) {
			//if connecting via websocket, might be stashed there
			tokenString = req.getHeader("Sec-WebSocket-Protocol");
			if (null == tokenString) {
				abortRequest(response, Response.SC_UNAUTHORIZED, "unauthorized");
				return;
			}
		}

		//split the token string into userid,series,token
		String[] pieces = tokenString.split("_");
		if (pieces.length != 3) {
			abortRequest(response, Response.SC_BAD_REQUEST, "auth header not in 3 pieces");
			return;
		}
		RCLoginTokenQueries dao = _dbi.onDemand(RCLoginTokenQueries.class);
		RCLoginToken token = dao.findToken(Integer.parseInt(pieces[0]), new BigInteger(pieces[2]));
		if (null == token) {
			abortRequest(response, Response.SC_UNAUTHORIZED, "failed to find token");
			return;
		}
		RCUser.Queries userDao = _dbi.onDemand(RCUser.Queries.class);
		RCUser user = userDao.findById(token.getUserId());
		if (null == user) {
			abortRequest(response, Response.SC_UNAUTHORIZED, "Failed to find user for token " + token.getId());
			return;
		}
		request.setAttribute(UserSessionKey, user);
		request.setAttribute(LoginTokenKey, token);
}

	private void abortRequest(ServletResponse rsp, int code, String error) throws IOException {
		HttpServletResponse response = (HttpServletResponse)rsp;
		response.setStatus(code);
		response.getWriter().println(error);
	}
}
