package org.realityforge.replicant.example.server.rest;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.realityforge.replicant.example.server.service.BadSessionException;
import org.realityforge.replicant.example.server.service.SubscriptionService;
import org.realityforge.replicant.shared.transport.ReplicantContext;

@Path( "/replicant" )
@Singleton
public class ReplicantResource
{
  @Inject
  private SubscriptionService _subscriptionService;

  @GET
  @Produces( MediaType.APPLICATION_JSON )
  public String poll( @HeaderParam( ReplicantContext.SESSION_ID_HEADER ) final String sessionID,
                      @QueryParam( "rx" ) final int rxSequence )
    throws BadSessionException
  {
    return _subscriptionService.poll( sessionID, rxSequence );
  }
}
