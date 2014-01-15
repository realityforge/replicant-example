package org.realityforge.replicant.example.client.services;

public interface DataLoaderService
{
  void connect();

  void disconnect();

  boolean isConnected();

  void subscribeToBuilding( int buildingID );

  void unsubscribeFromBuilding( int buildingID );
}
