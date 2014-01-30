package org.realityforge.replicant.example.client.services;

import javax.annotation.Nonnull;
import org.realityforge.replicant.example.client.entity.TyrellRemoteSubscriptionManager;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManager;
import org.realityforge.replicant.example.client.entity.TyrellSubscriptionManagerImpl;

public class TyrellClientSession
{
  private final String _sessionID;
  private final TyrellSubscriptionManager _subscriptionManager;

  public TyrellClientSession( @Nonnull final String sessionID,
                              @Nonnull final TyrellRemoteSubscriptionManager subscriptionManager )
  {
    _sessionID = sessionID;
    _subscriptionManager = new TyrellSubscriptionManagerImpl( subscriptionManager );
  }

  public String getSessionID()
  {
    return _sessionID;
  }

  public TyrellSubscriptionManager getSubscriptionManager()
  {
    return _subscriptionManager;
  }
}
