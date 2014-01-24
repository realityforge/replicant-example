package org.realityforge.replicant.example.client.services.replicant;

import javax.annotation.Nullable;

class SubscriptionEntry
{
  private final int _type;
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

  SubscriptionEntry( final int type, final Object id )
  {
    _type = type;
    _id = id;
  }

  int getType()
  {
    return _type;
  }

  @Nullable
  Object getId()
  {
    return _id;
  }

  boolean isPresent()
  {
    return _present;
  }

  void markAsPresent()
  {
    _present = true;
  }

  boolean isDeregisterInProgress()
  {
    return _deregisterInProgress;
  }

  void markDeregisterInProgress()
  {
    _deregisterInProgress = true;
  }
}
