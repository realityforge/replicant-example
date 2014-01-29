package org.realityforge.replicant.client.transport;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class AbstractSubscriptionManager<T extends Enum>
{
  //Graph => InstanceID
  private final HashMap<T, Map<Object, SubscriptionEntry<T>>> _instanceSubscriptions = new HashMap<>();
  //Graph => Type
  private final HashMap<T, SubscriptionEntry<T>> _typeSubscriptions = new HashMap<>();

  protected final SubscriptionEntry<T> subscribeToTypeGraph( final T graph )
  {
    SubscriptionEntry<T> typeMap = _typeSubscriptions.get( graph );
    if ( null == typeMap )
    {
      final SubscriptionEntry<T> entry = new SubscriptionEntry<>( graph, null );
      _typeSubscriptions.put( graph, entry );
      return entry;
    }
    else
    {
      return null;
    }
  }

  protected final SubscriptionEntry<T> subscribeToInstanceGraph( final T graph, @Nonnull final Object id )
  {
    Map<Object, SubscriptionEntry<T>> instanceMap = _instanceSubscriptions.get( graph );
    if ( null == instanceMap )
    {
      instanceMap = new HashMap<>();
      _instanceSubscriptions.put( graph, instanceMap );
    }
    if ( !instanceMap.containsKey( id ) )
    {
      final SubscriptionEntry<T> entry = new SubscriptionEntry<>( graph, id );
      instanceMap.put( id, entry );
      return entry;
    }
    else
    {
      return null;
    }
  }

  protected final SubscriptionEntry<T> unsubscribeFromTypeGraph( final T graph )
  {
    final SubscriptionEntry<T> entry = _typeSubscriptions.remove( graph );
    if ( null != entry )
    {
      entry.markDeregisterInProgress();
      return entry;
    }
    else
    {
      return null;
    }
  }

  protected final SubscriptionEntry<T> unsubscribeFromInstanceGraph( final T graph, @Nonnull final Object id )
  {
    final Map<Object, SubscriptionEntry<T>> instanceMap = _instanceSubscriptions.get( graph );
    if ( null == instanceMap )
    {
      return null;
    }
    final SubscriptionEntry<T> entry = instanceMap.remove( id );
    if ( null != entry )
    {
      entry.markDeregisterInProgress();
      return entry;
    }
    else
    {
      return null;
    }
  }
}
