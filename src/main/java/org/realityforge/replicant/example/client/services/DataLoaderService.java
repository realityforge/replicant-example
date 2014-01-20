package org.realityforge.replicant.example.client.services;

public interface DataLoaderService
{
  void connect();

  void disconnect();

  void subscribeToBuilding( int buildingID );

  void unsubscribeFromBuilding( int buildingID );

  void subscribeToAll();

  void downloadAll();
}
