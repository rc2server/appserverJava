package edu.wvu.stat.rc2.resources;

import java.security.SecureRandom;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.NotEmpty;
import org.mindrot.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.wvu.stat.rc2.persistence.RCFileQueries;
import edu.wvu.stat.rc2.persistence.RCLoginToken;
import edu.wvu.stat.rc2.persistence.RCLoginTokenQueries;
import edu.wvu.stat.rc2.persistence.RCUser;
import edu.wvu.stat.rc2.persistence.RCWorkspace;
import edu.wvu.stat.rc2.persistence.Rc2DAO;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource extends BaseResource {

	final static Logger log= LoggerFactory.getLogger("rc2.LoginResource");
	private static final SecureRandom random = new SecureRandom();
	
	public LoginResource() {
		super();
		System.err.println("login created");
	}

	public LoginResource(Rc2DAO dao, RCUser user) {
		super(dao, user);
	}

	@GET
	public Response checkLogin() {
		RCUser user = getUser();
		if (null == user)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		return Response.ok(new LoginOutput(user, _dao, getLoginToken().getCookieValue())).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response loginUser(@Valid LoginInput input) {
		RCUser user = getDAO().getUserDao().findByLogin(input.getLogin());
		if (user == null || !user.isEnabled() || !BCrypt.checkpw(input.getPassword(), user.getHashedPassword()))
			throw new WebApplicationException(Response.Status.UNAUTHORIZED);
		
		RCLoginTokenQueries tokenDao = _dao.getDBI().onDemand(RCLoginTokenQueries.class);
		RCLoginToken token = tokenDao.createToken(user.getId(), random.nextLong(), random.nextLong());
		
		NewCookie me = new NewCookie("me", token.getCookieValue(), "/", "", "", NewCookie.DEFAULT_MAX_AGE, true);
		return Response.ok(new LoginOutput(user, _dao, token.getCookieValue())).cookie(me).build();
	}
	
	static class LoginInput {
		final String _login;
		final String _password;
		
		@JsonCreator
		public LoginInput(@JsonProperty("login") String login, @JsonProperty("password") String password) {
			_login = login;
			_password = password;
		}
		
		public @NotEmpty String getLogin() { return _login; }
		public @NotEmpty @Size(min=4) String getPassword() { return _password; }
	}
	
	static class LoginOutput {
		final RCUser _user;
		final String _token;
		final List<RCWorkspace> _wspaces;
		
		LoginOutput(RCUser user, Rc2DAO dao, String token) {
			_user = user;
			_token = token;
			_wspaces = dao.getWorkspaceDao().ownedByUser(user.getId());
			RCFileQueries fileDao = dao.getDBI().onDemand(RCFileQueries.class);
			for (RCWorkspace wspace : _wspaces) {
				wspace.setFiles(fileDao.filesForWorkspaceId(wspace.getId()));
			}
		}
		
		public RCUser getUser() { return _user; }
		public String getToken() { return _token; }
		public List<RCWorkspace> getWorkspaces() { return _wspaces; }
		
	}
}
