package org.realityforge.replicant.example.server.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.ejb.EJB;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.mvc.Viewable;
import org.realityforge.ssf.HttpUtil;
import org.realityforge.ssf.SessionInfo;
import org.realityforge.ssf.SessionManager;

@Path("/auth")
@Produces({ MediaType.TEXT_HTML })
public class AuthenticationService
{
  private static final Logger LOG = Logger.getLogger( AuthenticationService.class.getName() );

  @EJB
  private SessionManager _sessionManager;

  @GET
  @Path("/login")
  public Response showLogin()
  {
    return Response.ok( new Viewable( "/auth/login.jsp" ) ).build();
  }

  @GET
  @Path("/token")
  public Response generateToken()
  {
    return Response.ok( _sessionManager.createSession().getSessionID() ).build();
  }

  @GET
  @Path("/logout")
  public Response logout( @Nonnull @Context final HttpServletRequest request )
  {
    final String sessionKey = _sessionManager.getSessionKey();
    final Cookie cookie = HttpUtil.findCookie( request, sessionKey );
    if ( null != cookie )
    {
      _sessionManager.invalidateSession( cookie.getValue() );
    }
    final NewCookie newCookie =
      newCookie( request, _sessionManager.getSessionKey(), null, false );
    return Response.ok( new Viewable( "/auth/logout.jsp" ) ).cookie( newCookie ).build();
  }

  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response authenticate( @FormParam("j_username") final String username,
                                @FormParam("j_password") final String password,
                                @Nonnull @Context final HttpServletRequest request )
    throws IOException, URISyntaxException
  {
    if ( authenticate( request, username, password ) )
    {
      final SessionInfo sessionInfo = _sessionManager.createSession();
      sessionInfo.setAttribute( "username",  username  );
      final URI uri = HttpUtil.getContextURI( request, getStartLocation() );
      final NewCookie newCookie =
        newCookie( request, _sessionManager.getSessionKey(), sessionInfo.getSessionID(), true );
      return Response.seeOther( uri ).cookie( newCookie ).build();
    }
    else
    {
      return Response.ok( new Viewable( "/auth/login.jsp", "error" ) ).build();
    }
  }

  protected NewCookie newCookie( final HttpServletRequest request,
                                 final String name,
                                 final String value,
                                 final boolean create )
  {
    return new NewCookie( name,
                          value,
                          request.getContextPath(),
                          null,
                          null,
                          create ? 1000 : 0,
                          false );
  }

  protected String getStartLocation()
  {
    return "/";
  }

  private boolean authenticate( final HttpServletRequest request, final String username, final String password )
  {
    final boolean authenticate = HttpUtil.authenticate( request, username, password );
    if ( authenticate )
    {
      LOG.info( "Successful authentication: " + username );
    }
    else
    {
      LOG.info( "Failed authentication: " + username );
    }
    return authenticate;
  }
}
