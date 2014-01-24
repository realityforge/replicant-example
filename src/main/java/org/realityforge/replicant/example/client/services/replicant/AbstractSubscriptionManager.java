package org.realityforge.replicant.example.client.services.replicant;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class AbstractSubscriptionManager
{
  private final HashMap<Integer, Map<Object, SubscriptionEntry>> _instanceSubscriptions = new HashMap<>();
  private final HashMap<Integer, SubscriptionEntry> _typeSubscriptions = new HashMap<>();
  private final RemoteSubscriptionManager _remoteSubscriptionManager;

  protected AbstractSubscriptionManager( final RemoteSubscriptionManager remoteSubscriptionManager )
  {
    _remoteSubscriptionManager = remoteSubscriptionManager;
  }

  protected final boolean subscribeToType( final int type )
  {
    checkCanSubscribeToType( type );
    if ( !_typeSubscriptions.containsKey( type ) )
    {
      final SubscriptionEntry entry = new SubscriptionEntry( type, null );
      _typeSubscriptions.put( type, entry );
      _remoteSubscriptionManager.remoteSubscribeToType( type, new Runnable()
      {
        @Override
        public void run()
        {
          entry.markAsPresent();
        }
      } );
      return true;
    }
    else
    {
      return false;
    }
  }

  protected final boolean subscribeToInstance( final int type, @Nonnull final Object id )
  {
    checkCanSubscribeToType( type );
    Map<Object, SubscriptionEntry> map = _instanceSubscriptions.get( type );
    if ( null == map )
    {
      map = new HashMap<>();
      _instanceSubscriptions.put( type, map );
    }
    if ( !map.containsKey( id ) )
    {
      final SubscriptionEntry entry = new SubscriptionEntry( type, id );
      map.put( id, entry );
      _remoteSubscriptionManager.remoteSubscribeToInstance( type, id, new Runnable()
      {
        @Override
        public void run()
        {
          entry.markAsPresent();
        }
      } );
      return true;
    }
    else
    {
      return false;
    }
  }

  protected final boolean unsubscribeFromType( final int type )
  {
    checkCanSubscribeToType( type );
    final SubscriptionEntry entry = _typeSubscriptions.get( type );
    if ( null != entry )
    {
      entry.markDeregisterInProgress();
      _remoteSubscriptionManager.remoteUnsubscribeFromType( type, new Runnable()
      {
        @Override
        public void run()
        {
          _typeSubscriptions.remove( type );
        }
      } );
      return true;
    }
    else
    {
      return false;
    }
  }

  protected final boolean unsubscribeFromInstance( final int type, @Nonnull final Object id )
  {
    checkCanSubscribeToType( type );
    final Map<Object, SubscriptionEntry> map = _instanceSubscriptions.get( type );
    if ( null == map )
    {
      return false;
    }
    final SubscriptionEntry entry = map.get( id );
    if ( null != entry )
    {
      entry.markDeregisterInProgress();
      _remoteSubscriptionManager.remoteUnsubscribeFromInstance( type, id, new Runnable()
      {
        @Override
        public void run()
        {
          map.remove( id );
        }
      } );
      return true;
    }
    else
    {
      return false;
    }
  }

  protected final void checkCanSubscribeToType( final int type )
  {
    if ( !_remoteSubscriptionManager.canSubscribeToType( type ) )
    {
      throw new IllegalStateException( "Attempted to subscribe to invalid type " + type );
    }
  }
}
