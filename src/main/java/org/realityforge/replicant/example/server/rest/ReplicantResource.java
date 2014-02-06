package org.realityforge.replicant.example.server.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import org.realityforge.replicant.example.server.service.BadSessionException;
import org.realityforge.replicant.example.server.service.SubscriptionService;
import org.realityforge.replicant.shared.transport.ReplicantContext;

@Path( "/replicant" )
@Singleton
public class ReplicantResource
{
  private final Map<AsyncResponse, Request> _requests =
    Collections.synchronizedMap( new HashMap<AsyncResponse, Request>() );
  private final ScheduledExecutorService _scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
  @EJB
  private SubscriptionService _subscriptionService;

  @PostConstruct
  public void postConstruct()
  {
    final Checker command = new Checker( _requests, _subscriptionService );
    _scheduledExecutor.scheduleWithFixedDelay( command, 0, 100, TimeUnit.MILLISECONDS );
  }

  @GET
  @Produces( "text/plain" )
  public void poll( @Suspended AsyncResponse response,
                    @Nonnull @HeaderParam( ReplicantContext.SESSION_ID_HEADER ) final String sessionID,
                    @QueryParam( "rx" ) final int rxSequence )
    throws BadSessionException
  {
    response.setTimeout( 2, TimeUnit.SECONDS );
    response.register( new ConnectionCallback()
    {
      @Override
      public void onDisconnect( final AsyncResponse disconnected )
      {
        _requests.remove( disconnected );
      }
    } );
    response.setTimeoutHandler( new TimeoutHandler()
    {
      @Override
      public void handleTimeout( final AsyncResponse asyncResponse )
      {
        asyncResponse.resume( "" );
      }
    } );

    try
    {
      final String data = _subscriptionService.poll( sessionID, rxSequence );
      if ( null != data )
      {
        response.resume( data );
      }
      else
      {
        _requests.put( response, new Request( sessionID, rxSequence, response ) );
      }
    }
    catch ( final BadSessionException e )
    {
      response.resume( e );
    }
  }

  static class Request
  {
    private final String _sessionID;
    private final int _rxSequence;
    private final AsyncResponse _response;

    Request( final String sessionID, final int rxSequence, final AsyncResponse response )
    {
      _sessionID = sessionID;
      _rxSequence = rxSequence;
      _response = response;
    }

    String getSessionID()
    {
      return _sessionID;
    }

    int getRxSequence()
    {
      return _rxSequence;
    }

    AsyncResponse getResponse()
    {
      return _response;
    }
  }

  static final class Checker
    implements Runnable
  {
    private final Map<AsyncResponse, Request> _requests;
    private final SubscriptionService _subscriptionService;

    Checker( final Map<AsyncResponse, Request> requests,
             final SubscriptionService subscriptionService )
    {
      _requests = requests;
      _subscriptionService = subscriptionService;
    }

    @Override
    public void run()
    {
      synchronized ( _requests )
      {
        final Iterator<Request> iterator = _requests.values().iterator();
        while ( iterator.hasNext() )
        {
          final Request request = iterator.next();

          if ( !request.getResponse().isSuspended() || request.getResponse().isCancelled() )
          {
            iterator.remove();
          }
          else
          {
            try
            {
              final String data = _subscriptionService.poll( request.getSessionID(), request.getRxSequence() );
              if ( null != data )
              {
                request.getResponse().resume( data );
                iterator.remove();
              }
            }
            catch ( final BadSessionException e )
            {
              request.getResponse().resume( e );
              iterator.remove();
            }
          }
        }
      }
    }
  }
}
