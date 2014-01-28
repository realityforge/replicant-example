package org.realityforge.replicant.example.client.services;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.realityforge.replicant.client.EntityRepository;
import org.realityforge.replicant.client.json.gwt.GwtDataLoaderService;
import org.realityforge.replicant.example.client.entity.Roster;
import org.realityforge.replicant.example.client.entity.Shift;
import org.realityforge.replicant.example.client.entity.TyrellRemoteSubscriptionManager;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManager;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManagerImpl;
import org.realityforge.replicant.example.client.event.SystemErrorEvent;
import org.realityforge.replicant.example.client.service.GwtRpcSubscriptionService;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncCallback;
import org.realityforge.replicant.example.client.service.TyrellGwtRpcAsyncErrorCallback;

public class TyrellDataLoaderService
  extends GwtDataLoaderService
  implements DataLoaderService, TyrellRemoteSubscriptionManager
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
    getSubscriptionManager().subscribeToMetaData();
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
  public void remoteSubscribeToMetaData( @Nonnull final Runnable runnable )
  {
    _subscriptionService.subscribeToMetaData( new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        if ( null != result )
        {
          enqueueDataLoad( false, result, runnable );
        }
      }
    } );
  }

  @Override
  public void remoteUnsubscribeFromMetaData( @Nonnull final Runnable runnable )
  {
    _subscriptionService.unsubscribeFromMetaData( new TyrellGwtRpcAsyncCallback<Void>()
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
    _subscriptionService.subscribeToRoster( id, new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        if ( null != result )
        {
          enqueueDataLoad( false, result, runnable );
        }
      }
    } );
  }

  @Override
  public void remoteUnsubscribeFromRoster( final int id, @Nonnull final Runnable runnable )
  {
    _subscriptionService.unsubscribeFromRoster( id, new TyrellGwtRpcAsyncCallback<Void>()
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
    _subscriptionService.subscribeToRosterList( new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        if ( null != result )
        {
          enqueueDataLoad( false, result, runnable );
        }
      }
    } );
  }

  @Override
  public void remoteUnsubscribeFromRosterList( @Nonnull final Runnable runnable )
  {
    _subscriptionService.unsubscribeFromRosterList( new TyrellGwtRpcAsyncCallback<Void>()
    {
      @Override
      public void onSuccess( final Void result )
      {
        unloadRosters();
        runnable.run();
      }
    } );
  }

  @Override
  public void subscribeToAll()
  {
    _subscriptionService.subscribeToAll( new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        enqueueDataLoad( false, result, null );
      }
    } );
  }

  @Override
  public void downloadAll()
  {
    _subscriptionService.downloadAll( new TyrellGwtRpcAsyncCallback<String>()
    {
      @Override
      public void onSuccess( final String result )
      {
        enqueueDataLoad( false, result, null );
      }
    } );
  }

  private void unloadRoster( final int rosterID )
  {
    final Roster roster = _repository.findByID( Roster.class, rosterID );
    if ( null != roster )
    {
      unloadRoster( roster );
    }
  }

  private void unloadRoster( final Roster roster )
  {
    for ( final Shift shift : new ArrayList<>( roster.getShifts() ) )
    {
      _repository.deregisterEntity( Shift.class, shift.getID() );
    }
    _repository.deregisterEntity( Roster.class, roster.getID() );
  }

  private void unloadRosters()
  {
    for ( final Roster roster : _repository.findAll( Roster.class ) )
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
