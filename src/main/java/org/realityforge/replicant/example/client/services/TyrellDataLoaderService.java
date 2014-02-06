package org.realityforge.replicant.example.client.services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.replicant.client.json.gwt.GwtDataLoaderService;
import org.realityforge.replicant.client.transport.CacheService;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;
import org.realityforge.replicant.example.client.entity.TyrellClientSession;
import org.realityforge.replicant.example.client.entity.TyrellClientSessionContext;
import org.realityforge.replicant.example.client.event.SystemErrorEvent;
import org.realityforge.replicant.example.client.service.GwtRpcSubscriptionService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.shared.transport.ReplicantContext;

public class TyrellDataLoaderService
  extends GwtDataLoaderService<TyrellClientSession>
  implements DataLoaderService
{
  private static final int POLL_DURATION = 2000;

  @Inject
  private EventBus _eventBus;
  @Inject
  private GwtRpcSubscriptionService _subscriptionService;

  private Timer _timer;
  private boolean _inPoll;

  @Override
  public void connect()
  {
    final String url = GWT.getHostPageBaseURL() + "api/auth/token";
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    try
    {
      builder.sendRequest( "", new RequestCallback()
      {
        @Override
        public void onResponseReceived( final Request request, final Response response )
        {
          onSessionCreated( response.getText() );
        }

        @Override
        public void onError( final Request request, final Throwable exception )
        {
          LOG.log( Level.SEVERE, "Error generating token", exception );
          Window.alert( "Failed to generate token" );
        }
      } );
    }
    catch ( final RequestException e )
    {
      LOG.log( Level.SEVERE, "Error generating token", e );
      Window.alert( "Failed to generate token" );
    }
  }

  private void onSessionCreated( final String sessionID )
  {
    setSession( new TyrellClientSession( sessionID, new Context() ), new Runnable()
    {
      @Override
      public void run()
      {
        startPolling();
        getSession().getSubscriptionManager().subscribeToMetaData();
      }
    } );
  }

  @Override
  public void disconnect()
  {
    stopPolling();
    setSession( null, null );
  }

  @Override
  public void downloadAll()
  {
    _subscriptionService.downloadAll( getSessionID() );
  }

  private void unloadRoster( final int rosterID )
  {
    final Roster roster = getRepository().findByID( Roster.class, rosterID );
    if ( null != roster )
    {
      unloadRoster( roster );
    }
  }

  private void unloadRoster( final Roster roster )
  {
    for ( final Shift shift : new ArrayList<>( roster.getShifts() ) )
    {
      getRepository().deregisterEntity( Shift.class, shift.getID() );
    }
    getRepository().deregisterEntity( Roster.class, roster.getID() );
  }

  private void unloadRosters()
  {
    for ( final Roster roster : getRepository().findAll( Roster.class ) )
    {
      unloadRoster( roster );
    }
  }

  private void poll()
  {
    if ( _inPoll )
    {
      return;
    }

    _inPoll = true;
    final TyrellClientSession session = getSession();

    final String baseURL = GWT.getHostPageBaseURL() + "api/replicant";
    final String url = baseURL + "?rx=" + session.getLastRxSequence();
    final RequestBuilder rb = new RequestBuilder( RequestBuilder.GET, url );
    rb.setHeader( ReplicantContext.SESSION_ID_HEADER, session.getSessionID() );
    try
    {

      rb.sendRequest( "", new RequestCallback()
      {
        @Override
        public void onResponseReceived( final Request request, final Response response )
        {
          _inPoll = false;
          final String rawJsonData = response.getText();
          if ( Response.SC_OK == response.getStatusCode() && 0 != rawJsonData.length() )
          {
            handlePollSuccess( rawJsonData );
          }
        }

        @Override
        public void onError( final Request request, final Throwable exception )
        {
          _inPoll = false;
          handleSystemFailure( exception, "Failed to poll" );
        }
      } );
    }
    catch ( final RequestException e )
    {
      LOG.log( Level.SEVERE, "Error initiating poll", e );
    }
  }

  final void handlePollSuccess( final String rawJsonData )
  {
    if ( null != rawJsonData )
    {
      if ( LOG.isLoggable( Level.SEVERE ) )
      {
        LOG.severe( "Received data from poll: " + rawJsonData );
      }
      enqueueDataLoad( rawJsonData );
    }
  }

  final void handleSystemFailure( final Throwable caught, final String message )
  {
    LOG.log( Level.SEVERE, "System Failure: " + message, caught );
    final Throwable cause = ( caught instanceof InvocationException ) ? caught.getCause() : caught;
    _eventBus.fireEvent( new SystemErrorEvent( message, cause ) );
  }

  private void startPolling()
  {
    stopPolling();
    _timer = new Timer()
    {
      @Override
      public void run()
      {
        poll();
      }
    };

    _timer.scheduleRepeating( POLL_DURATION );
  }

  private void stopPolling()
  {
    if ( null != _timer )
    {
      _timer.cancel();
      _timer = null;
    }
  }

  @Override
  protected void progressDataLoadFailure( @Nonnull final Exception e )
  {
    handleSystemFailure( e, "Failed to progress data load" );
  }

  class Context
    implements TyrellClientSessionContext
  {
    @Override
    public CacheService getCacheService()
    {
      return TyrellDataLoaderService.this.getCacheService();
    }

    @Override
    public void loadCachedContent( @Nonnull final String changeSet,
                                   @Nonnull final Runnable runnable,
                                   final boolean bulkChange )
    {
      enqueueOOB( changeSet, runnable, bulkChange );
    }

    @Override
    public void remoteSubscribeToMetaData( @Nullable final String eTag,
                                           @Nonnull final Runnable cacheCurrentAction,
                                           @Nonnull final Runnable runnable )
    {
      _subscriptionService.subscribeToMetaData( getSessionID(), eTag, new TyrellGwtRpcAsyncCallback<Boolean>()
      {
        @Override
        public void onSuccess( final Boolean result )
        {
          if ( result )
          {
            runnable.run();
          }
          else
          {
            cacheCurrentAction.run();
          }
        }
      } );
    }

    @Override
    public void remoteUnsubscribeFromMetaData( @Nonnull final Runnable runnable )
    {
      _subscriptionService.unsubscribeFromMetaData( getSessionID(), new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteSubscribeToRoster( final int id, @Nonnull final Runnable runnable )
    {
      _subscriptionService.subscribeToRoster( getSessionID(), id, new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteUnsubscribeFromRoster( final int id, @Nonnull final Runnable runnable )
    {
      _subscriptionService.unsubscribeFromRoster( getSessionID(), id, new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          unloadRoster( id );
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteSubscribeToRosterList( @Nonnull final Runnable runnable )
    {
      _subscriptionService.subscribeToRosterList( getSessionID(), new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteUnsubscribeFromRosterList( @Nonnull final Runnable runnable )
    {
      _subscriptionService.unsubscribeFromRosterList( getSessionID(), new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          unloadRosters();
          runnable.run();
        }
      } );
    }
  }
}
