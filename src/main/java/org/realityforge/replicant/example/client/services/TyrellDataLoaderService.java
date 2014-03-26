package org.realityforge.replicant.example.client.services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.gwt.webpoller.client.AbstractHttpRequestFactory;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.WebPollerListenerAdapter;
import org.realityforge.replicant.client.ChangeMapper;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.EntitySubscriptionManager;
import org.realityforge.replicant.client.transport.CacheService;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.client.net.AbstractTyrellDataLoaderService;
import org.realityforge.replicant.example.client.net.TyrellClientSessionImpl;
import org.realityforge.replicant.example.client.event.SessionEstablishedEvent;
import org.realityforge.replicant.example.client.event.SystemErrorEvent;
import org.realityforge.replicant.example.client.service.internal.GwtSubscriptionService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.example.shared.entity.TyrellReplicationGraph;
import org.realityforge.replicant.shared.transport.ReplicantContext;

public class TyrellDataLoaderService
  extends AbstractTyrellDataLoaderService
  implements DataLoaderService
{
  private final EventBus _eventBus;
  private final GwtSubscriptionService _subscriptionService;

  private final WebPoller _webPoller = WebPoller.newWebPoller();

  class ReplicantRequestFactory
    extends AbstractHttpRequestFactory
  {
    @Override
    protected RequestBuilder getRequestBuilder()
    {
      final RequestBuilder rb = new RequestBuilder( RequestBuilder.GET, getPollURL() );
      rb.setHeader( ReplicantContext.SESSION_ID_HEADER, getSessionID() );
      return rb;
    }
  }

  @Inject
  public TyrellDataLoaderService( final ChangeMapper changeMapper,
                                  final EntityChangeBroker changeBroker,
                                  final EntityRepository repository,
                                  final CacheService cacheService,
                                  final EntitySubscriptionManager subscriptionManager,
                                  final EventBus eventBus,
                                  final GwtSubscriptionService subscriptionService )
  {
    super( changeMapper, changeBroker, repository, cacheService, subscriptionManager );
    _eventBus = eventBus;
    _subscriptionService = subscriptionService;
    _webPoller.setListener( new WebPollerListenerAdapter()
    {
      @Override
      public void onMessage( @Nonnull final WebPoller webPoller,
                             @Nonnull final Map<String, String> context,
                             @Nonnull final String data )
      {
        handlePollSuccess( data );
      }

      @Override
      public void onError( @Nonnull final WebPoller webPoller, @Nonnull final Throwable exception )
      {
        handleSystemFailure( exception, "Failed to poll" );
      }
    } );
  }

  @Override
  public void connect()
  {
    final String url = GWT.getHostPageBaseURL() + "api/auth/token";
    final RequestBuilder rb = new RequestBuilder( RequestBuilder.GET, url );
    try
    {
      rb.sendRequest( "", new RequestCallback()
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
    setSession( new TyrellClientSessionImpl( this, sessionID, getRepository(), getChangeBroker() ),
                new Runnable()
                {
                  @Override
                  public void run()
                  {
                    getSession().subscribeToMetaData( new Runnable()
                    {
                      @Override
                      public void run()
                      {
                        _eventBus.fireEvent( new SessionEstablishedEvent() );
                      }
                    } );
                  }
                } );
    scheduleDataLoad();
    startPolling();
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

  private String getPollURL()
  {
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final TyrellClientSessionImpl session = getSession();

    final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.length() - moduleName.length() - 1 );
    final String suffix = "?rx=" + session.getLastRxSequence();

    return contextURL + "api/replicant" + suffix;
  }

  final void handlePollSuccess( final String rawJsonData )
  {
    if ( null != rawJsonData )
    {
      if ( LOG.isLoggable( Level.SEVERE ) )
      {
        LOG.severe( "Received data from poll: " + rawJsonData );
      }
      getSession().enqueueDataLoad( rawJsonData );
      if ( !_webPoller.isPaused() )
      {
        _webPoller.pause();
      }
    }
  }

  @Override
  protected void onDataLoadComplete( final boolean bulkLoad, @Nullable final String requestID )
  {
    if ( _webPoller.isPaused() )
    {
      _webPoller.resume();
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
    _webPoller.setRequestFactory( new ReplicantRequestFactory() );
    _webPoller.setLongPoll( true );
    _webPoller.start();
  }

  private void stopPolling()
  {
    if ( isConnected() )
    {
      _webPoller.stop();
    }
  }

  public boolean isConnected()
  {
    return _webPoller.isActive();
  }

  @Override
  protected void progressDataLoadFailure( @Nonnull final Exception e )
  {
    handleSystemFailure( e, "Failed to progress data load" );
  }

  @Override
  protected void requestSubscribeToGraph( @Nonnull final TyrellReplicationGraph graph,
                                          @Nullable final Object id,
                                          @Nullable final Object filterParameter,
                                          @Nullable final String eTag,
                                          @Nullable final Runnable cacheAction,
                                          @Nonnull final Runnable completionAction )
  {
    final TyrellGwtRpcAsyncCallback<Void> callback = new TyrellGwtRpcAsyncCallback<Void>()
    {
      @Override
      public void onSuccess( final Void result )
      {
        completionAction.run();
      }
    };
    if ( TyrellReplicationGraph.SHIFT == graph )
    {
      _subscriptionService.subscribeToShift( getSessionID(), (Integer) id, callback );
    }
    else if ( TyrellReplicationGraph.ROSTER_LIST == graph )
    {
      _subscriptionService.subscribeToRosterList( getSessionID(), callback );
    }
    else if ( TyrellReplicationGraph.SHIFT_LIST == graph )
    {
      _subscriptionService.subscribeToShiftList( getSessionID(),
                                                 (Integer) id,
                                                 (RosterSubscriptionDTO) filterParameter,
                                                 callback );
    }
    else if ( TyrellReplicationGraph.META_DATA == graph )
    {
      _subscriptionService.subscribeToMetaData( getSessionID(), eTag, new TyrellGwtRpcAsyncCallback<Boolean>()
      {
        @Override
        public void onSuccess( final Boolean result )
        {
          if ( result )
          {
            completionAction.run();
          }
          else
          {
            if ( null != cacheAction )
            {
              cacheAction.run();
            }
          }
        }
      } );
    }
  }

  @Override
  protected void requestUnsubscribeFromGraph( @Nonnull final TyrellReplicationGraph graph,
                                              @Nullable final Object id,
                                              @Nonnull final Runnable completionAction )
  {
    final TyrellGwtRpcAsyncCallback<Void> callback = new TyrellGwtRpcAsyncCallback<Void>()
    {
      @Override
      public void onSuccess( final Void result )
      {
        completionAction.run();
      }
    };
    if ( TyrellReplicationGraph.SHIFT == graph )
    {
      _subscriptionService.unsubscribeFromShift( getSessionID(), (Integer) id, callback );
    }
    else if ( TyrellReplicationGraph.ROSTER_LIST == graph )
    {
      _subscriptionService.unsubscribeFromRosterList( getSessionID(), callback );
    }
    else if ( TyrellReplicationGraph.SHIFT_LIST == graph )
    {
      _subscriptionService.unsubscribeFromShiftList( getSessionID(), (Integer) id, callback );
    }
    else if ( TyrellReplicationGraph.META_DATA == graph )
    {
      _subscriptionService.unsubscribeFromMetaData( getSessionID(), callback );
    }
  }

  @Override
  protected void requestUpdateSubscription( @Nonnull final TyrellReplicationGraph graph,
                                            @Nullable final Object id,
                                            @Nullable final Object filterParameter,
                                            @Nonnull final Runnable completionAction )
  {
    final TyrellGwtRpcAsyncCallback<Void> callback = new TyrellGwtRpcAsyncCallback<Void>()
    {
      @Override
      public void onSuccess( final Void result )
      {
        completionAction.run();
      }
    };
    if ( TyrellReplicationGraph.SHIFT_LIST == graph )
    {
      _subscriptionService.updateSubscriptionToShiftList( getSessionID(),
                                                          (Integer) id,
                                                          (RosterSubscriptionDTO) filterParameter,
                                                          callback );
    }
  }
}
