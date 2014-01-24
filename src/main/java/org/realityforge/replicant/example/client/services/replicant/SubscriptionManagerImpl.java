package org.realityforge.replicant.example.client.services.replicant;

import org.realityforge.replicant.example.client.entity.Roster;

public class SubscriptionManagerImpl
  extends AbstractSubscriptionManager
  implements SubscriptionManager
{
  public SubscriptionManagerImpl( final RemoteSubscriptionManager remoteSubscriptionManager )
  {
    super( remoteSubscriptionManager );
  }

  @Override
  public void subscribeToRoster( final int rosterID )
  {
    subscribeToInstance( Roster.TRANSPORT_ID, rosterID );
  }

  @Override
  public void unsubscribeFromRoster( final int rosterID )
  {
    unsubscribeFromInstance( Roster.TRANSPORT_ID, rosterID );
  }

  @Override
  public void subscribeToAllRosters()
  {
    subscribeToType( Roster.TRANSPORT_ID );
  }

  @Override
  public void unsubscribeFromAllRosters()
  {
    unsubscribeFromType( Roster.TRANSPORT_ID );
  }
}
