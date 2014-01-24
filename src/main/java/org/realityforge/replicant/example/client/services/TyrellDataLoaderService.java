package org.realityforge.replicant.example.client.services;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.web.bindery.event.shared.EventBus;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.json.gwt.GwtDataLoaderService;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManager;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManagerImpl;
import org.realityforge.replicant.example.client.event.BulkLoadCompleteEvent;
import org.realityforge.replicant.example.client.event.IncrementalLoadCompleteEvent;
import org.realityforge.replicant.example.client.event.SystemErrorEvent;
import org.realityforge.replicant.example.client.service.GwtRpcSubscriptionService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncErrorCallback;
import org.realityforge.replicant.example.client.services.replicant.RemoteSubscriptionManager;

public class TyrellDataLoaderService
  extends GwtDataLoaderService
  implements DataLoaderService, RemoteSubscriptionManager
{
  private static final int POLL_DURATION = 2000;

  @Inject
  private EventBus _eventBus;
  @Inject
  private GwtRpcSubscriptionService _subscriptionService;
  @Inject
  private EntityRepository _repository;

  private final TyrellSubscriptionManager _subscriptionManager = new TyrellSubscriptionManagerImpl( this );

  private Timer _timer;

  private boolean _inPoll;
  private boolean _incrementalDataLoadInProgress;

  @Override
  public void connect()
  {
    startPolling();
  }

  @Override
  public void disconnect()
  {
    stopPolling();
  }

  @Override
  public TyrellSubscriptionManager getSubscriptionManager()
  {
    return _subscriptionManager;
  }

  @Override
  public void remoteSubscribeToType( final int type, @Nonnull final Runnable runnable )
  {
    _subscriptionService.subscribeToAll( new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        enqueueDataLoad( false, result, runnable );
      }
    } );
  }

  @Override
  public void remoteUnsubscribeFromType( final int type, @Nonnull final Runnable runnable )
  {

  }

  @Override
  public void remoteSubscribeToInstance( final int type, @Nonnull final Object id, @Nonnull final Runnable runnable )
  {
    _subscriptionService.subscribeToRoster( (Integer) id, new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        enqueueDataLoad( false, result, runnable );
      }
    } );
  }

  @Override
  public void remoteUnsubscribeFromInstance( final int type,
                                             @Nonnull final Object id,
                                             @Nonnull final Runnable runnable )
  {
    final Integer buildingID = (Integer) id;
    _subscriptionService.unsubscribeFromRoster( buildingID, new TyrellGwtRpcAsyncCallback<Void>()
    {
      @Override
      public void onSuccess( final Void result )
      {
        unloadBuilding( buildingID );
        runnable.run();
      }
    } );
  }

  public void subscribeToRoster( final int rosterID )
  {
    _subscriptionManager.subscribeToRoster( rosterID );
  }

  @Override
  public void downloadAll()
  {
    _subscriptionService.downloadAll( new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        enqueueDataLoad( true, result, null );
      }
    } );
  }

  private void unloadBuilding( final int buildingID )
  {
    final Roster roster = _repository.findByID( Roster.class, buildingID );
    if ( null != roster )
    {
      for ( final Shift shift : roster.getShifts() )
      {
        _repository.deregisterEntity( Shift.class, shift.getID() );
      }
      _repository.deregisterEntity( Roster.class, buildingID );
    }
  }

  private void poll()
  {
    if ( _inPoll )
    {
      return;
    }

    _inPoll = true;
    _subscriptionService.
      poll( getLastKnownChangeSet(),
            new TyrellGwtRpcAsyncCallback<String>()
            {
              @Override
              public void onSuccess( final String rawJsonData )
              {
                _inPoll = false;
                handlePollSuccess( rawJsonData );
              }
            },
            new TyrellGwtRpcAsyncErrorCallback()
            {
              @Override
              public void onFailure( final Throwable caught )
              {
                _inPoll = false;
                handleSystemFailure( caught, "Failed to poll" );
              }
            }
      );
  }

  final void handlePollSuccess( final String rawJsonData )
  {
    if ( null != rawJsonData )
    {
      if ( LOG.isLoggable( Level.SEVERE ) )
      {
        LOG.severe( "Received data from poll: " + rawJsonData );
      }
      enqueueDataLoad( false, rawJsonData, null );
    }
  }

  final void handleSystemFailure( final Throwable caught, final String message )
  {
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
  protected void onBulkLoadComplete()
  {
    _eventBus.fireEvent( new BulkLoadCompleteEvent() );
  }

  @Override
  protected void onIncrementalLoadComplete()
  {
    _eventBus.fireEvent( new IncrementalLoadCompleteEvent() );
  }

  protected void scheduleDataLoad()
  {
    if ( !_incrementalDataLoadInProgress )
    {
      _incrementalDataLoadInProgress = true;
      Scheduler.get().scheduleIncremental( new Scheduler.RepeatingCommand()
      {
        @Override
        public boolean execute()
        {
          try
          {
            _incrementalDataLoadInProgress = progressDataLoad();
          }
          catch ( final Exception e )
          {
            handleSystemFailure( e, "Failed to progress data load" );
            return false;
          }
          return _incrementalDataLoadInProgress;
        }
      } );
    }
  }
}
