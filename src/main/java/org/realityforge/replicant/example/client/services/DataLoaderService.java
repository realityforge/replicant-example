package org.realityforge.replicant.example.client.services;

public interface DataLoaderService
{
  void subscribeToBuilding( int buildingID );

  void unsubscribeFromBuilding( int buildingID );
}
