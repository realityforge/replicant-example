package org.realityforge.replicant.example.server.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.realityforge.replicant.example.server.service.BadSessionException;

@Provider
public class BadSessionExceptionMapper
  implements ExceptionMapper<BadSessionException>
{
  public Response toResponse( final BadSessionException nre )
  {
    return Response.
      status( Status.FORBIDDEN ).
      entity( "Invalid Session" ).
      type( MediaType.TEXT_PLAIN ).
      build();
  }
}
