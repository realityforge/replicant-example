package org.realityforge.replicant.example.client.services.replicant;

import javax.annotation.Nonnull;

public interface RemoteSubscriptionManager
{
  void remoteSubscribeToType( int type, @Nonnull Runnable runnable );

  void remoteUnsubscribeFromType( int type, @Nonnull Runnable runnable );

  void remoteSubscribeToInstance( int type, @Nonnull Object id, @Nonnull Runnable runnable );

  void remoteUnsubscribeFromInstance( int type, @Nonnull Object id, @Nonnull Runnable runnable );
}
