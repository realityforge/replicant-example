package org.realityforge.replicant.example.client.services.replicant;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class AbstractSubscriptionManager
{
  //Mode => Type => InstanceID
  private final HashMap<Enum, Map<Integer, Map<Object, SubscriptionEntry>>> _instanceSubscriptions = new HashMap<>();
  //Mode => Type
  private final HashMap<Enum, HashMap<Integer, SubscriptionEntry>> _typeSubscriptions = new HashMap<>();
  private final RemoteSubscriptionManager _remoteSubscriptionManager;

  protected AbstractSubscriptionManager( final RemoteSubscriptionManager remoteSubscriptionManager )
  {
    _remoteSubscriptionManager = remoteSubscriptionManager;
  }

  protected final boolean subscribeToType( final Enum mode, final int type )
  {
    HashMap<Integer, SubscriptionEntry> typeMap = _typeSubscriptions.get( mode );
    if ( null == typeMap )
    {
      typeMap = new HashMap<>();
      _typeSubscriptions.put( mode, typeMap );
    }
    if ( !typeMap.containsKey( type ) )
    {
      final SubscriptionEntry entry = new SubscriptionEntry( type, null );
      typeMap.put( type, entry );
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

  protected final boolean subscribeToInstance( final Enum mode, final int type, @Nonnull final Object id )
  {
    Map<Integer, Map<Object, SubscriptionEntry>> instanceMap = _instanceSubscriptions.get( mode );
    if ( null == instanceMap )
    {
      instanceMap = new HashMap<>();
      _instanceSubscriptions.put( mode, instanceMap );
    }
    Map<Object, SubscriptionEntry> map = instanceMap.get( type );
    if ( null == map )
    {
      map = new HashMap<>();
      instanceMap.put( type, map );
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

  protected final boolean unsubscribeFromType( final Enum mode, final int type )
  {
    final HashMap<Integer, SubscriptionEntry> typeMap = _typeSubscriptions.get( mode );
    if ( null == typeMap )
    {
      return false;
    }
    final SubscriptionEntry entry = typeMap.get( type );
    if ( null != entry )
    {
      entry.markDeregisterInProgress();
      _remoteSubscriptionManager.remoteUnsubscribeFromType( type, new Runnable()
      {
        @Override
        public void run()
        {
          typeMap.remove( type );
        }
      } );
      return true;
    }
    else
    {
      return false;
    }
  }

  protected final boolean unsubscribeFromInstance( final Enum mode, final int type, @Nonnull final Object id )
  {
    final Map<Integer, Map<Object, SubscriptionEntry>> instanceMap = _instanceSubscriptions.get( mode );
    if ( null == instanceMap )
    {
      return false;
    }
    final Map<Object, SubscriptionEntry> map = instanceMap.get( type );
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
}
