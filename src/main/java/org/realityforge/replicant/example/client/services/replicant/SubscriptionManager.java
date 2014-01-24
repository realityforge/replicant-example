package org.realityforge.replicant.example.client.services.replicant;

public interface SubscriptionManager
{
  void subscribeToRoster( int rosterID );

  void unsubscribeFromRoster( int rosterID );

  void subscribeToAllRosters();

  void unsubscribeFromAllRosters();
}
