package edu.wvu.stat.rc2.ws;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wvu.stat.rc2.persistence.RCUser;

import static edu.wvu.stat.rc2.Rc2AppConfiguration.*;

import java.io.IOException;

public class RCSessionServlet extends WebSocketServlet {
	private static final long serialVersionUID = 1L;
	static final Logger log = LoggerFactory.getLogger("RCSessionServlet");

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.setCreator(new RC2WebSocketCreator());
	}

	class RC2WebSocketCreator implements WebSocketCreator {

		@Override
		public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
			log.info("createWebSocket request rcvd:" + req);
			try {
				RCUser user = (RCUser) req.getHttpServletRequest().getAttribute(UserSessionKey);
				if (null == user) {
					resp.sendError(HttpStatus.BAD_REQUEST_400, "user not authenticated");
					return null;
				}
				log.info("websocket got user " + user.getLogin());
				
			} catch (IOException ioe) {
				//don't care, should never happen
				log.warn("ioe creating websocket", ioe);
			}
			return null;
		}
	}
}
