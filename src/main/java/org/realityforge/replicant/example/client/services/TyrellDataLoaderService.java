package org.realityforge.replicant.example.client.services;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.logging.Level;
import javax.inject.Inject;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.json.gwt.GwtDataLoaderService;
import org.realityforge.replicant.example.client.entity.tyrell.Building;
import org.realityforge.replicant.example.client.entity.tyrell.Room;
import org.realityforge.replicant.example.client.event.tyrell.BuildingDataLoadedEvent;
import org.realityforge.replicant.example.client.event.tyrell.BulkLoadCompleteEvent;
import org.realityforge.replicant.example.client.event.tyrell.IncrementalLoadCompleteEvent;
import org.realityforge.replicant.example.client.event.tyrell.SystemErrorEvent;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncErrorCallback;
import org.realityforge.replicant.example.client.service.tyrell.GwtRpcSubscriptionService;

public class TyrellDataLoaderService
  extends GwtDataLoaderService
  implements DataLoaderService
{
  private static final int POLL_DURATION = 2000;

  @Inject
  private EventBus _eventBus;
  @Inject
  private GwtRpcSubscriptionService _subscriptionService;
  @Inject
  private EntityRepository _repository;

  private Timer _timer;

  private boolean _inPoll;
  private boolean _incrementalDataLoadInProgress;
  private HashSet<Integer> _subscriptions = new HashSet<>();

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
  public boolean isConnected()
  {
    return null != _timer;
  }

  public void subscribeToBuilding( final int buildingID )
  {
    if ( !_subscriptions.contains( buildingID ) )
    {
      //TODO: Need to track time whilst subscription updating
      _subscriptions.add( buildingID );
      _subscriptionService.subscribeToBuilding( buildingID, new TyrellGwtRpcAsyncCallback<String>()
      {
        @Override
        public void onSuccess( final String result )
        {
          onLoadBuildingData( result );
        }
      } );
    }
  }

  @Override
  public void unsubscribeFromBuilding( final int buildingID )
  {
    if ( _subscriptions.contains( buildingID ) )
    {
      //TODO: Need to track time whilst subscription updating
      _subscriptions.remove( buildingID );
      _subscriptionService.unsubscribeFromBuilding( buildingID, new TyrellGwtRpcAsyncCallback<Void>()
      {
        @Override
        public void onSuccess( final Void result )
        {
          unloadBuilding( buildingID );
        }
      } );
    }
  }

  private void unloadBuilding( final int buildingID )
  {
    final Building building = _repository.findByID( Building.class, buildingID );
    if ( null != building )
    {
      for ( final Room room : building.getRooms() )
      {
        _repository.deregisterEntity( Room.class, room.getID() );
      }
      _repository.deregisterEntity( Building.class, buildingID );
    }
  }

  private void onLoadBuildingData( final String rawJsonData )
  {
    final Runnable runnable = new Runnable()
    {
      public void run()
      {
        _eventBus.fireEvent( new BuildingDataLoadedEvent() );
      }
    };
    enqueueDataLoad( true, rawJsonData, runnable );
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
