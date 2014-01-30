package org.realityforge.replicant.example.client.services;

import javax.annotation.Nonnull;
import org.realityforge.replicant.client.transport.ClientSession;
import org.realityforge.replicant.example.client.entity.TyrellRemoteSubscriptionManager;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManager;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManagerImpl;

public class TyrellClientSession
  extends ClientSession
{
  private final TyrellSubscriptionManager _subscriptionManager;

  public TyrellClientSession( @Nonnull final String sessionID,
                              @Nonnull final TyrellRemoteSubscriptionManager subscriptionManager )
  {
    super( sessionID );
    _subscriptionManager = new TyrellSubscriptionManagerImpl( subscriptionManager );
  }

  public TyrellSubscriptionManager getSubscriptionManager()
  {
    return _subscriptionManager;
  }
}
