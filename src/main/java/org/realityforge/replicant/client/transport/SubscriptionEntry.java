package org.realityforge.replicant.client.transport;

import javax.annotation.Nullable;

public class SubscriptionEntry<T extends Enum>
{
  private final T _graph;
  @Nullable
  private final Object _id;

  /**
   * True if the initial load of data for entity has been downloaded and is local.
   */
  private boolean _present;
  /**
   * True if the subscription is part way through being cancelled.
   */
  private boolean _deregisterInProgress;
  /**
   * True if a deregister has not been completed.
   */
  private boolean _registered;

  public SubscriptionEntry( final T graph, final Object id )
  {
    _graph = graph;
    _id = id;
    _registered = true;
  }

  public T getGraph()
  {
    return _graph;
  }

  @Nullable
  public Object getId()
  {
    return _id;
  }

  public boolean isPresent()
  {
    return _present;
  }

  public void markAsPresent()
  {
    _present = true;
  }

  public boolean isDeregisterInProgress()
  {
    return _deregisterInProgress;
  }

  public void markDeregisterInProgress()
  {
    _deregisterInProgress = true;
  }

  public void markAsDeregistered()
  {
    _registered = false;
  }
}
