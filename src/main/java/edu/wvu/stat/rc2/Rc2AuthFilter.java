package edu.wvu.stat.rc2;

import java.io.IOException;
import java.math.BigInteger;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.persistence.PGDataSourceFactory;
import edu.wvu.stat.rc2.persistence.RCLoginToken;
import edu.wvu.stat.rc2.persistence.RCLoginTokenQueries;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.rs.Rc2SecurityContext;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class Rc2AuthFilter implements ContainerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(Rc2AuthFilter.class);

	private DBI _dbi;
	
	public Rc2AuthFilter(PGDataSourceFactory factory) {
		_dbi = factory.createDBI();
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		final String path = requestContext.getUriInfo().getPath();
		//always allow login requests via POST
		if (path.equals("login") && requestContext.getMethod().equals("POST"))
			return;

		//for robots, return a basic string saying everything is disallowed
		if (path.equals("/robots.txt")) {
			requestContext.abortWith(Response.ok().entity("User-agent: *\nDisallow: /\n").build());
		}

		//check for auth header first
		String tokenString = requestContext.getHeaderString("RC2-Auth");
		if (null == tokenString) {
			//fall back to auth cookie
			Cookie cookie = requestContext.getCookies().get("me");
			if (null == cookie || null == cookie.getValue()) {
				abortRequest(requestContext, "me cooking missing");
				return;
			}
			tokenString = cookie.getValue();
		}
		//split the token string into userid,series,token
		String[] pieces = tokenString.split("_");
		if (pieces.length != 3) {
			abortRequest(requestContext, "auth header not in 3 pieces");
			return;
		}
		RCLoginTokenQueries dao = _dbi.onDemand(RCLoginTokenQueries.class);
		RCLoginToken token = dao.findToken(Integer.parseInt(pieces[0]), new BigInteger(pieces[2]));
		if (null == token) {
			abortRequest(requestContext, "failed to find token");
			return;
		}
		RCUser.Queries userDao = _dbi.onDemand(RCUser.Queries.class);
		RCUser user = userDao.findById(token.getUserId());
		if (null == user) {
			abortRequest(requestContext, "Failed to find user for token " + token.getId());
			return;
		}
		requestContext.setProperty("user", user);
		requestContext.setSecurityContext(new Rc2SecurityContext(user, token));
	}

	private void abortRequest(ContainerRequestContext requestContext, String error) {
		log.info("authoriziation error: " + error);
		requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid authentiction info").build());
		
	}
	
}
