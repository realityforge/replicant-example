package org.realityforge.replicant.example.client.services.replicant;

import javax.annotation.Nonnull;

public interface SubscriptionManager
{
  boolean subscribeToType( int type );

  boolean subscribeToInstance( int type, @Nonnull Object id );

  boolean unsubscribeFromType( int type );

  boolean unsubscribeFromInstance( int type, @Nonnull Object id );
}
