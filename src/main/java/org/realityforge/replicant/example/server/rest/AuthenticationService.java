package org.realityforge.replicant.example.server.rest;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.realityforge.ssf.SessionManager;

@Path("/auth")
@Produces({ MediaType.TEXT_HTML })
public class AuthenticationService
{
  @EJB
  private SessionManager _sessionManager;

  @GET
  @Path("/token")
  public Response generateToken()
  {
    return Response.ok( _sessionManager.createSession().getSessionID() ).build();
  }
}
