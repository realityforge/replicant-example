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
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.realityforge.gwt.webpoller.client.AbstractHttpRequestFactory;
import org.realityforge.gwt.webpoller.client.WebPoller;
import org.realityforge.gwt.webpoller.client.event.ErrorEvent;
import org.realityforge.gwt.webpoller.client.event.MessageEvent;
import org.realityforge.replicant.client.EntityChangeBroker;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.json.gwt.GwtDataLoaderService;
import org.realityforge.replicant.client.transport.CacheService;
import org.realityforge.replicant.example.client.data_type.RosterSubscriptionDTO;
import org.realityforge.replicant.example.client.entity.TyrellClientSession;
import org.realityforge.replicant.example.client.entity.TyrellClientSessionContext;
import org.realityforge.replicant.example.client.event.SessionEstablishedEvent;
import org.realityforge.replicant.example.client.event.SystemErrorEvent;
import org.realityforge.replicant.example.client.service.GwtRpcSubscriptionService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.shared.transport.ReplicantContext;

/**
 * TODO: Rework the generated to accept arbitrary parameters subscriptionManager
 */
public class TyrellDataLoaderService
  extends GwtDataLoaderService<TyrellClientSession>
  implements DataLoaderService
{
  @Inject
  private EventBus _eventBus;
  @Inject
  private GwtRpcSubscriptionService _subscriptionService;

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

  public TyrellDataLoaderService()
  {
    registerListeners();
  }

  private void registerListeners()
  {
    _webPoller.addMessageHandler( new MessageEvent.Handler()
    {
      @Override
      public void onMessageEvent( @Nonnull final MessageEvent event )
      {
        handlePollSuccess( event.getData() );
      }
    } );
    _webPoller.addErrorHandler( new ErrorEvent.Handler()
    {
      @Override
      public void onErrorEvent( @Nonnull final ErrorEvent event )
      {
        handleSystemFailure( event.getException(), "Failed to poll" );
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
    setSession( new TyrellClientSession( sessionID, new Context() ), new Runnable()
    {
      @Override
      public void run()
      {
        startPolling();
        getSession().getSubscriptionManager().subscribeToMetaData( null );
        _eventBus.fireEvent( new SessionEstablishedEvent() );
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

  private String getPollURL()
  {
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final TyrellClientSession session = getSession();

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
      enqueueDataLoad( rawJsonData );
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

  class Context
    implements TyrellClientSessionContext
  {

    @Override
    public EntityChangeBroker getEntityChangeBroker()
    {
      return TyrellDataLoaderService.this.getChangeBroker();
    }

    @Override
    public EntityRepository getRepository()
    {
      return TyrellDataLoaderService.this.getRepository();
    }

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
    public void remoteSubscribeToShift( final int id, @Nonnull final Runnable runnable )
    {
      _subscriptionService.subscribeToShift( getSessionID(), id, new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteUnsubscribeFromShift( final int id, @Nonnull final Runnable runnable )
    {
      _subscriptionService.unsubscribeFromShift( getSessionID(), id, new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
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
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteSubscribeToShiftList( final int id,
                                            @Nonnull final RosterSubscriptionDTO filter,
                                            @Nonnull final Runnable runnable )
    {
      _subscriptionService.subscribeToShiftList( getSessionID(), id, filter, new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteUnsubscribeFromShiftList( final int id, @Nonnull final Runnable runnable )
    {
      _subscriptionService.unsubscribeFromShiftList( getSessionID(), id, new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          runnable.run();
        }
      } );
    }

    @Override
    public void remoteUpdateShiftListSubscription( final int id,
                                                   @Nonnull final RosterSubscriptionDTO filter,
                                                   @Nonnull final Runnable runnable )
    {
      _subscriptionService.updateSubscriptionToShiftList( getSessionID(), id, filter,
                                                          new TyrellGwtRpcAsyncCallback<Void>()
                                                          {
                                                            @Override
                                                            public void onSuccess( final Void result )
                                                            {
                                                              runnable.run();
                                                            }
                                                          } );
    }
  }
}
